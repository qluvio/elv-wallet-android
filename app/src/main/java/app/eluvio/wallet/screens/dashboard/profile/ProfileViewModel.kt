package app.eluvio.wallet.screens.dashboard.profile

import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.Environment
import app.eluvio.wallet.data.EnvironmentStore
import app.eluvio.wallet.data.FabricConfigStore
import app.eluvio.wallet.data.UserStore
import app.eluvio.wallet.util.userId
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userStore: UserStore,
    private val fabricConfigStore: FabricConfigStore,
    private val environmentStore: EnvironmentStore,
) : BaseViewModel<ProfileViewModel.State>(State()) {
    data class State(
        val address: String = "",
        val userId: String = "",
        val network: Environment? = null,
        val fabricNode: String = ""
    )

    override fun onStart() {
        super.onStart()

        Observable.combineLatest(
            environmentStore.observeSelectedEnvironment(),
            fabricConfigStore.observeFabricConfiguration(),
            userStore.getCurrentUser().toObservable()
        ) { env, config, user ->
            State(
                address = user.address,
                userId = user.userId,
                network = env,
                fabricNode = config.endpoint
            )
        }
            .subscribeBy { state ->
                updateState { state }
            }
            .addTo(disposables)
    }
}
