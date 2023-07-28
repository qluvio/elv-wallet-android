package app.eluvio.wallet.data.stores

import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.network.api.authd.AuthServicesApi
import app.eluvio.wallet.network.api.authd.AuthTokenResponse
import app.eluvio.wallet.network.api.authd.MetamaskActivationData
import app.eluvio.wallet.network.api.authd.MetamaskCodeRequest
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.rx.mapNotNull
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class MetamaskActivationStore @Inject constructor(
    private val apiProvider: ApiProvider,
    private val tokenStore: TokenStore,
    private val environmentStore: EnvironmentStore,
    private val moshi: Moshi,
) {

    fun observeMetamaskActivationData(): Flowable<MetamaskActivationData> {
        return apiProvider.getApi(AuthServicesApi::class).flatMapPublisher { api ->
            environmentStore.observeSelectedEnvironment()
                .map { api to it }
        }
            .switchMap { (api, env) ->
                observeMetamaskActivationData(api, env.walletUrl)
            }
    }

    private fun observeMetamaskActivationData(
        api: AuthServicesApi,
        walletUrl: String
    ): Flowable<MetamaskActivationData> {
        return api.generateMetamaskCode(MetamaskCodeRequest(walletUrl))
            .toFlowable()
            // Make observable never-ending so we can restart it even after getting successful result from auth0
            .mergeWith(Flowable.never())
            .timeout {
                val delay = (it.expiration * 1000 - Calendar.getInstance().timeInMillis)
                    .coerceIn(1.minutes.inWholeMilliseconds..7.minutes.inWholeMilliseconds)
                Flowable.timer(delay, TimeUnit.MILLISECONDS)
                    .doOnComplete {
                        Log.d("ActivationData timeout reached, re-fetching metamask activation data")
                    }
            }
            .retry()
    }

    /**
     * Only returns a value when activation is complete and fabric token has been obtained.
     * Otherwise returns an empty [Maybe].
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun checkToken(activationData: MetamaskActivationData): Maybe<AuthTokenResponse> {
        return apiProvider.getApi(AuthServicesApi::class)
            .flatMap { api -> api.getMetamaskToken(activationData.code, activationData.passcode) }
            .mapNotNull { httpResponse ->
                Log.d("check token result $httpResponse")
                httpResponse.body()?.let { metamaskTokenResponse ->
                    moshi.adapter<AuthTokenResponse>().fromJson(metamaskTokenResponse.payload)
                }
            }
            .doOnSuccess {
                val token = it.token
                tokenStore.fabricToken = token
                tokenStore.walletAddress = it.address
            }
    }
}
