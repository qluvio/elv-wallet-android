package app.eluvio.wallet.app

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import app.eluvio.wallet.BuildConfig
import app.eluvio.wallet.data.stores.FabricConfigStore
import app.eluvio.wallet.di.TokenAwareHttpClient
import app.eluvio.wallet.util.logging.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
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
    lateinit var installReferrerHandler: InstallReferrerHandler

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(configRefresher)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        installReferrerHandler.captureInstallReferrer()
    }

    override fun newImageLoader(): ImageLoader {
        // Coil checks if Application implements ImageLoaderFactory and calls this automatically.
        // We provide our own OkHttpClient so image requests include fabric token headers.
        return ImageLoader.Builder(this).okHttpClient(httpClient)
            .components { add(ContentFabricSizingInterceptor()) }
            .build()
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
