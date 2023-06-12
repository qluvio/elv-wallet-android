package app.eluvio.wallet.screens.envselect

import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.stores.Environment
import app.eluvio.wallet.data.stores.EnvironmentStore
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import javax.inject.Inject

@HiltViewModel
class EnvSelectViewModel @Inject constructor(
    private val environmentStore: EnvironmentStore
) : BaseViewModel<EnvSelectViewModel.State>(State()) {
    data class State(
        val loading: Boolean = true,
        val availableEnvironments: List<Environment> = Environment.values().toList(),
        val selectedEnvironment: Environment? = null,
    )

    override fun onResume() {
        super.onResume()
        environmentStore.observeSelectedEnvironment()
            .subscribe { newEnv ->
                updateState {
                    copy(loading = false, selectedEnvironment = newEnv)
                }
            }
            .addTo(disposables)
    }

    fun selectEnvironment(environment: Environment) {
        environmentStore.setSelectedEnvironment(environment)
    }
}