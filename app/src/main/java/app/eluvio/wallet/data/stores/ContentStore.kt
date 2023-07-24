package app.eluvio.wallet.data.stores

import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.NftEntity
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.network.api.authd.GatewayApi
import app.eluvio.wallet.network.converters.toNfts
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.realm.asFlowable
import app.eluvio.wallet.util.realm.saveTo
import app.eluvio.wallet.util.rx.mapNotNull
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import javax.inject.Inject


class ContentStore @Inject constructor(
    private val apiProvider: ApiProvider,
    private val realm: Realm,
) {

    fun observeWalletData(): Flowable<Result<List<NftEntity>>> {
        return realm.query<NftEntity>().asFlowable()
            .map { Result.success(it) }
            .mergeWith(
                fetchWalletData()
                    .mapNotNull { nfts ->
                        // TODO: this is a hack to double-emit an empty list when network returns empty so that the viewmodel can stop the loading state.
                        // fetchWalletData() should be a completable that doesn't emit items.
                        Result.success(nfts).takeIf { nfts.isEmpty() }
                    }
                    .onErrorReturn { Result.failure(it) }
            )
            .doOnNext {
                it.exceptionOrNull()?.let { error ->
                    Log.e("Error in wallet data stream", error)
                }
            }
    }

    fun observeNft(contractAddress: String, tokenId: String): Flowable<NftEntity> {
        return realm.query<NftEntity>(
            "${NftEntity::contractAddress.name} == $0 && ${NftEntity::tokenId.name} == $1",
            contractAddress,
            tokenId
        ).asFlowable()
            .mapNotNull { it.firstOrNull() }
    }

    fun observeMediaItem(mediaId: String): Flowable<MediaEntity> {
        return realm.query<MediaEntity>(
            "${MediaEntity::id.name} == $0",
            mediaId
        ).asFlowable()
            .mapNotNull { it.firstOrNull() }
    }

    private fun fetchWalletData(): Single<List<NftEntity>> {
        return apiProvider.getApi(GatewayApi::class)
            .flatMap { api -> api.getNfts() }
            .map { response -> response.toNfts() }
            .saveTo(realm, clearTable = true)
    }
}
