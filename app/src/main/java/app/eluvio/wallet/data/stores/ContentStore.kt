package app.eluvio.wallet.data.stores

import app.eluvio.wallet.data.converters.toNfts
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.NftEntity
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.di.getApi
import app.eluvio.wallet.network.GatewayApi
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.mapNotNull
import app.eluvio.wallet.util.realm.asFlowable
import app.eluvio.wallet.util.realm.saveTo
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.realm.kotlin.Realm
import javax.inject.Inject


class ContentStore @Inject constructor(
    private val fabricConfigStore: FabricConfigStore,
    private val apiProvider: ApiProvider,
    private val realm: Realm,
) {

    fun observeWalletData(): Flowable<Result<List<NftEntity>>> {
        return realm.query(NftEntity::class).asFlowable()
            .map { Result.success(it) }
            .mergeWith(fetchWalletData().onErrorReturn { Result.failure(it) })
            .doOnNext {
                it.exceptionOrNull()?.let { error ->
                    Log.e("Error in wallet data stream", error)
                }
            }
    }

    fun observeNft(contractAddress: String): Flowable<List<NftEntity>> {
        return realm.query(
            NftEntity::class,
            "${NftEntity::contractAddress.name} == $0",
            contractAddress
        ).asFlowable()
    }

    fun observeMediaItems(): Flowable<List<MediaEntity>> {
        return realm.query(MediaEntity::class).asFlowable()
    }

    fun observeMediaItem(mediaId: String): Flowable<MediaEntity> {
        return realm.query(
            MediaEntity::class,
            "${MediaEntity::id.name} == $0",
            mediaId
        ).asFlowable()
            .mapNotNull { it.firstOrNull() }
    }

    private fun fetchWalletData(): Completable {
        return fabricConfigStore.observeFabricConfiguration()
            .firstOrError()
            .flatMap { config ->
                // For unsecured http, add this to the manifest: android:usesCleartextTraffic="true"
                // val authBaseUrl = "http://192.168.90.175:8080/as"
                val authBaseUrl = config.endpoint
                val url = "${authBaseUrl}/$WALLET_DATA_PATH"
                Log.w("tryna get nfts from $url")
                apiProvider.getApi<GatewayApi>()
                    .flatMap { api -> api.getNfts(url) }
                    .map { response -> response.toNfts(config) }
            }
            .saveTo(realm)
            .ignoreElement()
    }

    companion object {
        private const val WALLET_DATA_PATH = "apigw/nfts"
    }
}
