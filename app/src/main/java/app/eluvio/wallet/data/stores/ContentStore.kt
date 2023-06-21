package app.eluvio.wallet.data.stores

import app.eluvio.wallet.data.converters.toNfts
import app.eluvio.wallet.data.entities.NftEntity
import app.eluvio.wallet.network.GatewayApi
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.realm.asFlowable
import app.eluvio.wallet.util.realm.saveTo
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.realm.kotlin.Realm
import javax.inject.Inject

class ContentStore @Inject constructor(
    private val fabricConfigStore: FabricConfigStore,
    private val gatewayApi: GatewayApi,
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

    private fun fetchWalletData(): Completable {
        return fabricConfigStore.observeFabricConfiguration()
            .firstOrError()
            .map { config ->
                // For unsecured http, add this to the manifest: android:usesCleartextTraffic="true"
                val authBaseUrl = "http://localhost:6546"
                // val authBaseUrl = config.network.services.authService.first()
                "${authBaseUrl}$WALLET_DATA_PATH"
            }
            .flatMap { url ->
                Log.w("tryna get nfts from $url")
                gatewayApi.getNfts(url)
            }
            .map { it.toNfts() }
            .saveTo(realm)
            .ignoreElement()
    }

    companion object {
        private const val WALLET_DATA_PATH = "/apigw/nfts"
    }
}
