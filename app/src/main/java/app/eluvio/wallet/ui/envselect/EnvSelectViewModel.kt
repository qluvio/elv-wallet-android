package app.eluvio.wallet.ui.envselect

import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.Environment
import app.eluvio.wallet.data.EnvironmentStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EnvSelectViewModel @Inject constructor(
    private val environmentStore: EnvironmentStore
) : BaseViewModel<EnvSelectViewModel.State>() {
    data class State(
        val loading: Boolean,
        val availableEnvironments: List<Environment>,
        val selectedEnvironment: Environment?
    )

    override val state = environmentStore.observeSelectedEnvironment()
        .scan(State(loading = true, Environment.values().toList(), null)) { state, newEnv ->
            state.copy(loading = false, selectedEnvironment = newEnv)
        }
        .replay(1)
        .refCount()
        .distinctUntilChanged()

    fun selectEnvironment(environment: Environment) {
        environmentStore.setSelectedEnvironment(environment)
    }
}