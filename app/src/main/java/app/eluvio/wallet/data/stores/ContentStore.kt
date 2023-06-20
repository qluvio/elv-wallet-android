package app.eluvio.wallet.data.stores

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.rx3.asObservable
import app.cash.sqldelight.rx3.mapToList
import app.eluvio.wallet.data.converters.toNfts
import app.eluvio.wallet.network.GatewayApi
import app.eluvio.wallet.sqldelight.Nft
import app.eluvio.wallet.sqldelight.NftQueries
import app.eluvio.wallet.util.logging.Log
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class ContentStore @Inject constructor(
    private val fabricConfigStore: FabricConfigStore,
    private val gatewayApi: GatewayApi,
    private val nftQueries: NftQueries,
) {

    fun observeWalletData(): Observable<Result<List<Nft>>> {
        return nftQueries.getAll().asObservable().mapToList()
            .map { Result.success(it) }
            .mergeWith(fetchWalletData().onErrorReturn { Result.failure(it) })
    }

    fun observeNft(contractAddress: String): Observable<List<Nft>> {
        return nftQueries.getByContractAddr(contractAddress).asObservable().mapToList()
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
            .transactOnEach(nftQueries) { insert(it) }
            .ignoreElement()
    }

    // supposed to help save list of items inside a transaction. maybe i'm over engineering
    fun <D : Any, T : Transacter> Single<List<D>>.transactOnEach(
        transacter: T,
        action: T.(D) -> Unit
    ): Single<List<D>> {
        return doOnSuccess { list ->
            transacter.transaction {
                list.forEach { action(transacter, it) }
            }
        }
    }

    companion object {
        private const val WALLET_DATA_PATH = "/apigw/nfts"
    }
}
