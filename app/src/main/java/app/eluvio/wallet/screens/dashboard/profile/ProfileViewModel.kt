package app.eluvio.wallet.screens.dashboard.profile

import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.Environment
import app.eluvio.wallet.data.EnvironmentStore
import app.eluvio.wallet.data.FabricConfigStore
import app.eluvio.wallet.data.TokenStore
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.kotlin.withLatestFrom
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenStore: TokenStore,
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

        environmentStore
            .observeSelectedEnvironment()
            .withLatestFrom(fabricConfigStore.observeFabricConfiguration())
            .subscribeBy { (env, config) ->
                updateState {
                    val address = tokenStore.accountId ?: "MISSING"
                    State(
                        address = address,
                        userId = "TBD",
                        network = env,
                        fabricNode = config.endpoint
                    )
                }
            }
            .addTo(disposables)
    }
}
