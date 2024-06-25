package app.eluvio.wallet.network.interceptors

import android.os.Build
import app.eluvio.wallet.BuildConfig
import app.eluvio.wallet.data.AuthenticationService
import app.eluvio.wallet.data.SignOutHandler
import app.eluvio.wallet.data.stores.TokenStore
import app.eluvio.wallet.network.api.Auth0Api
import app.eluvio.wallet.network.api.RefreshTokenRequest
import app.eluvio.wallet.util.logging.Log
import dagger.Lazy
import io.reactivex.rxjava3.processors.BehaviorProcessor
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This interceptor is responsible for adding the appropriate token to each request.
 * As well as trying to refresh the token if it's expired, and retrying the request if the refresh is successful.
 */
@Singleton
class AccessTokenInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
    private val auth0Api: Auth0Api,
    private val authenticationService: Lazy<AuthenticationService>,
    private val signOutHandler: SignOutHandler,
) : Interceptor {

    private val userAgent = if (BuildConfig.DEBUG) {
        // Don't use "androidtv" in debug build, to bypass eligible-tenants filtering by backend.
        "android-debug_v${BuildConfig.VERSION_NAME}"
    } else {
        "AndroidTV/${Build.VERSION.RELEASE} (api:${Build.VERSION.SDK_INT}; ${BuildConfig.APPLICATION_ID}:${BuildConfig.VERSION_NAME}_${BuildConfig.VERSION_CODE}; ${Build.MANUFACTURER}; ${Build.MODEL}; ${Build.DEVICE})"
    }

    /** URLs that contain these paths should not get special handling for expired tokens. */
    private val authRequestPaths = setOf("wlt/login/jwt", "wlt/sign/eth")

    /**
     * Keeps track of whether or not a token is currently being refreshed.
     * This is to prevent multiple calls from trying to refresh at the same time.
     */
    private val refreshInProgress = BehaviorProcessor.createDefault(false)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val response = chain.proceed(request.withHeaders())
        val isAuthRelatedRequest = authRequestPaths.any { request.url.toString().contains(it) }
        if (isAuthRelatedRequest) {
            log("Auth related requested, no special handling")
            return response
        }

        return when (response.code) {
            HttpURLConnection.HTTP_UNAUTHORIZED,
            HttpURLConnection.HTTP_FORBIDDEN -> {
                // If there's no new response from refreshAndRetry, then we should return the original response.
                return refreshAndRetry(chain) ?: run {
                    signOutHandler
                        .signOut("Token expired. Please sign in again.")
                        .blockingAwait()
                    return@run response
                }
            }

            else -> response
        }
    }

    /**
     * Potentially refreshes the token and retries the request.
     * Returns a new response in case of successful refresh+retry, or null no refreshToken is available or refresh fails.
     */
    private fun refreshAndRetry(chain: Interceptor.Chain): Response? {
        val request = chain.request()
        log("Detected bad token from ${request.url}")
        val refreshToken = tokenStore.refreshToken.get()
        if (refreshToken == null) {
            log("No refresh token to work with (metamask user?). Signing out.")
            // Can't refresh token, so this is a fatal error.
            return null
        }
        // Wait till refresh is done before proceeding
        if (refreshInProgress.value == true) {
            log("another refresh call in-flight. waiting...")
            refreshInProgress
                .distinctUntilChanged()
                .takeUntil { refreshInProgress -> !refreshInProgress }
                .blockingSubscribe()
            log("done waiting!")

            if (refreshToken != tokenStore.refreshToken.get()) {
                // refresh token has changed since we started this request. Someone else must have refreshed it.
                log("refresh token changed. retrying original request.")
                return chain.proceed(request.withHeaders())
            }
        }


        log("going to actually try to refresh token")
        refreshInProgress.onNext(true)
        // Clear out invalid tokens
        tokenStore.update(
            tokenStore.clusterToken to null,
            tokenStore.fabricToken to null,
        )
        return runCatching {
            // try to refresh
            auth0Api.refreshToken(RefreshTokenRequest(refreshToken = refreshToken))
                .blockingGet()
        }
            .mapCatching {
                log("refresh call successful, making new fabric token")
                tokenStore.update(
                    tokenStore.idToken to it.idToken,
                    tokenStore.accessToken to it.accessToken,
                    tokenStore.refreshToken to it.refreshToken,
                )

                val newFabricToken =
                    authenticationService.get().getFabricToken().blockingGet()
                log("Successfully refreshed token. Retrying original request (new token=$newFabricToken)")
                refreshInProgress.onNext(false)
                // retry original request with new tokens
                chain.proceed(request.withHeaders())
            }
            .onFailure {
                log("Failed to refresh token. Signing out.", it)
                refreshInProgress.onNext(false)
            }
            .getOrNull()
    }

    /**
     * Creates a copy of the request with all the headers we need.
     */
    private fun Request.withHeaders(): Request {
        val builder = newBuilder()
        builder.header("User-Agent", userAgent)
        builder.header("Accept", "*/*") // needed for link/file resolution from the fabric
        addTokenHeader(url.toString(), builder)
        return builder.build()
    }

    /**
     * Adds a token header appropriate for the given request URL and token availability.
     */
    private fun addTokenHeader(requestUrl: String, builder: Request.Builder) {
        if (requestUrl.contains("wlt/login/jwt")) {
            tokenStore.idToken.get()?.let { idToken ->
                builder.header("Authorization", "Bearer $idToken")
            }
        } else if (requestUrl.contains("wlt/sign/eth")) {
            tokenStore.clusterToken.get()?.let { walletToken ->
                builder.header("Authorization", "Bearer $walletToken")
            }
        } else {
            tokenStore.fabricToken.get()?.let { fabricToken ->
                builder.header("Authorization", "Bearer $fabricToken")
            }
        }
    }

    private fun log(message: String, throwable: Throwable? = null) {
        if (ENABLE_EXCESSIVE_LOGGING) {
            Log.d(message, throwable)
        }
    }
}

// The process of refreshing a token is flaky and complicated.
// Logs are helpful for debugging, but they're too verbose to be enabled by default.
private const val ENABLE_EXCESSIVE_LOGGING = false
