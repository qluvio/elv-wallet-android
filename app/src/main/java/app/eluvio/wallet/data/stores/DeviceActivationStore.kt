package app.eluvio.wallet.data.stores

import app.eluvio.wallet.network.api.Auth0Api
import app.eluvio.wallet.network.api.DeviceActivationData
import app.eluvio.wallet.network.api.GetTokenRequest
import app.eluvio.wallet.network.api.GetTokenResponse
import app.eluvio.wallet.util.logging.Log
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DeviceActivationStore @Inject constructor(
    private val auth0Api: Auth0Api,
    private val tokenStore: TokenStore,
) {
    fun observeActivationData(): Observable<DeviceActivationData> {
        return auth0Api.getAuth0ActivationData()
            .doOnSuccess {
                Log.d("Auth0 activation data fetched: $it")
            }
            .toObservable()
            // Make observable never-ending so we can restart it even after getting successful result from auth0
            .mergeWith(Observable.never())
            .map {
                // [it.verificationUri] is correct, but not pretty. Hardcoding short link
                it.copy(verificationUri = "https://elv.lv/activate")
            }
            .timeout {
                Observable.timer(it.expiresInSeconds, TimeUnit.SECONDS)
                    .doOnComplete {
                        Log.d("ActivationData timeout reached, re-fetching from auth0")
                    }
            }
            .retry()
    }

    fun checkToken(deviceCode: String): Single<Response<GetTokenResponse>> {
        return auth0Api.getToken(GetTokenRequest(deviceCode = deviceCode))
            .doOnSuccess {
                Log.d("check token result $it")
                val response = it.body()
                tokenStore.idToken = response?.idToken
                tokenStore.accessToken = response?.accessToken
                tokenStore.refreshToken = response?.refreshToken
            }
    }
}
