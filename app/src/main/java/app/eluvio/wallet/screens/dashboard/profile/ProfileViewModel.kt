package app.eluvio.wallet.screens.dashboard.profile

import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.SignOutHandler
import app.eluvio.wallet.data.stores.Environment
import app.eluvio.wallet.data.stores.EnvironmentStore
import app.eluvio.wallet.data.stores.FabricConfigStore
import app.eluvio.wallet.data.stores.UserStore
import app.eluvio.wallet.navigation.asNewRoot
import app.eluvio.wallet.screens.NavGraphs
import app.eluvio.wallet.util.sqldelight.userId
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
    private val signOutHandler: SignOutHandler,
) : BaseViewModel<ProfileViewModel.State>(State()) {
    data class State(
        val address: String = "",
        val userId: String = "",
        val network: Environment? = null,
        val fabricNode: String = ""
    )

    override fun onResume() {
        super.onResume()

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

    fun signOut() {
        signOutHandler.signOut()
        navigateTo(NavGraphs.preLaunchGraph.asNewRoot())
    }
}
