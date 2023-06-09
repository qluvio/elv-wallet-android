package app.eluvio.wallet.app

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import app.eluvio.wallet.data.stores.FabricConfigStore
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

@HiltAndroidApp
class WalletApplication : Application() {
    @Inject
    lateinit var configRefresher: ConfigRefresher
    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(configRefresher)
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