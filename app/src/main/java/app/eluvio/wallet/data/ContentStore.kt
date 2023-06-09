package app.eluvio.wallet.data

import app.eluvio.wallet.network.GatewayApi
import app.eluvio.wallet.network.NftResponse
import app.eluvio.wallet.util.logging.Log
import io.reactivex.rxjava3.core.Maybe
import javax.inject.Inject

class ContentStore @Inject constructor(
    private val fabricConfigStore: FabricConfigStore,
    private val userStore: UserStore,
    private val gatewayApi: GatewayApi,
) {
    fun getWalletData(): Maybe<NftResponse> {
        return fabricConfigStore.observeFabricConfiguration()
            .firstOrError()
            .flatMapMaybe { config ->
                userStore.getCurrentUser().map { user ->
                    // For unsecured http, add this to the manifest: android:usesCleartextTraffic="true"
                    val authBaseUrl = "http://localhost:6546"
//                    val authBaseUrl = config.network.services.authService.first()
                    "${authBaseUrl}${WALLET_DATA_PATH}${user.address}"
                }
                    .doOnError { Log.e("gw error", it) }
            }
            .flatMapSingle { url ->
                Log.w("tryna get nfts from $url tok=$")
                gatewayApi.getNfts(url)
            }
    }

    companion object {
        private const val WALLET_DATA_PATH = "/apigw/nfts/"
    }
}
