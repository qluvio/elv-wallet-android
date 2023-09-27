package app.eluvio.wallet.app

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import app.eluvio.wallet.BuildConfig
import app.eluvio.wallet.data.stores.DeeplinkStore
import app.eluvio.wallet.data.stores.FabricConfigStore
import app.eluvio.wallet.di.TokenAwareHttpClient
import app.eluvio.wallet.util.logging.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse
import com.android.installreferrer.api.InstallReferrerClient.newBuilder
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.rxjava3.disposables.Disposable
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class WalletApplication : Application(), ImageLoaderFactory {
    @Inject
    lateinit var configRefresher: ConfigRefresher

    @Inject
    @TokenAwareHttpClient
    lateinit var httpClient: OkHttpClient

    @Inject
    lateinit var deeplinkStore: DeeplinkStore
    private lateinit var referrerClient: InstallReferrerClient

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(configRefresher)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        if (!deeplinkStore.installRefHandled) {
            referrerClient = newBuilder(this).build()
            referrerClient.startConnection(object : InstallReferrerStateListener {

                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    when (responseCode) {
                        InstallReferrerResponse.OK -> {
                            // Connection established.
                            Log.d("Install referrer connection established")
                            val response: ReferrerDetails = referrerClient.installReferrer
                            handleInstallReferrerResponse(response)
                            referrerClient.endConnection()
                        }

                        InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                            // API not available on the current Play Store app.
                            Log.e("InstallReferrer: feature not supported")
                        }

                        InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                            // Connection couldn't be established.
                            Log.e("InstallReferrer: service unavailable")
                        }
                    }
                }

                override fun onInstallReferrerServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            })
        }
    }

    private fun handleInstallReferrerResponse(response: ReferrerDetails) {
        val referrerUrl: String = response.installReferrer
        Log.d("Install referrerUrl: $referrerUrl")
        val params = runCatching {
            referrerUrl.split("=", "&", "%26", "%3D")
                .chunked(2)
                .associate { it[0] to it[1] }
        }.getOrDefault(emptyMap())
        val url = params["url"]
        url?.let {
            val regex = Regex(
                pattern = "https://eluv\\.io/deeplinkdemo/marketplace/([A-Za-z0-9]+)/sku/([A-Za-z0-9]+)\\?jwt=([A-Za-z0-9]+)",
                options = setOf(RegexOption.IGNORE_CASE)
            )
            regex.find(it)
        }?.destructured?.let { (marketplace, sku, jwt) ->
            deeplinkStore.installRefHandled = true
            deeplinkStore.deeplinkRequest =
                DeeplinkStore.DeeplinkRequest(marketplace, sku, jwt)
        }
    }

    override fun newImageLoader(): ImageLoader {
        // Coil checks if Application implements ImageLoaderFactory and calls this automatically.
        // We provide our own OkHttpClient so image requests include fabric token headers.
        return ImageLoader.Builder(this).okHttpClient(httpClient).build()
    }
}

class ConfigRefresher @Inject constructor(
    private val fabricConfigStore: FabricConfigStore
) : DefaultLifecycleObserver {
    private var disposable: Disposable? = null
    override fun onStart(owner: LifecycleOwner) {
        Log.d("============APP START=========")
        disposable = fabricConfigStore.observeFabricConfiguration()
            .ignoreElements()
            .retry()
            .subscribe()
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d("============APP STOP=========")
        disposable?.dispose()
        disposable = null
    }
}
