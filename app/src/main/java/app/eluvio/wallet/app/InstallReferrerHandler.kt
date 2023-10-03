package app.eluvio.wallet.app

import android.content.Context
import app.eluvio.wallet.data.stores.DeeplinkStore
import app.eluvio.wallet.screens.home.HomeViewModel
import app.eluvio.wallet.util.logging.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val DEEPLINK_URL_REGEX_PATTERN =
    "https://(?:www.)?eluv\\.io/deeplinkdemo/marketplace/([A-Za-z0-9]+)/sku/([A-Za-z0-9]+)\\?jwt=([A-Za-z0-9]+)"

/**
 * Looks for Install Referrer parameters (https://developer.android.com/google/play/installreferrer/library).
 * If found, parses the URL and stores the deeplink request in [DeeplinkStore] to be handled by the app (see [HomeViewModel]).
 */
class InstallReferrerHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deeplinkStore: DeeplinkStore
) {
    fun captureInstallReferrer() {
        if (deeplinkStore.installRefHandled) {
            // Only needs to happen once per app install
            return
        }
        val referrerClient = InstallReferrerClient.newBuilder(context).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        // Connection established. Check for deeplink parameters.
                        Log.d("Install referrer connection established")
                        handleInstallReferrerResponse(referrerClient.installReferrer)
                    }

                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        // API not available on the current Play Store app.
                        Log.e("InstallReferrer: feature not supported")
                    }

                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        // Connection couldn't be established.
                        Log.e("InstallReferrer: service unavailable")
                    }
                }

                // Regardless of result, never invoke InstallReferrerClient again.
                deeplinkStore.installRefHandled = true
                referrerClient.endConnection()
            }

            override fun onInstallReferrerServiceDisconnected() {}
        })
    }

    private fun handleInstallReferrerResponse(response: ReferrerDetails) {
        val installReferrerParams = response.installReferrer
        Log.d("Install installReferrerParams: $installReferrerParams")
        val parametersAsMap = runCatching {
            installReferrerParams.split("=", "&", "%26", "%3D")
                .chunked(2)
                .associate { it[0] to it[1] }
        }
            .getOrDefault(emptyMap())

        parametersAsMap["url"]
            ?.let {
                val regex = Regex(
                    pattern = DEEPLINK_URL_REGEX_PATTERN,
                    options = setOf(RegexOption.IGNORE_CASE)
                )
                regex.find(it)
            }
            ?.destructured?.let { (marketplace, sku, jwt) ->
                deeplinkStore.deeplinkRequest =
                    DeeplinkStore.DeeplinkRequest(marketplace, sku, jwt)
            }
    }
}
