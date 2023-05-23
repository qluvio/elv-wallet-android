package app.eluvio.wallet.ui.envselect

import androidx.lifecycle.ViewModel
import app.eluvio.wallet.data.Environment
import app.eluvio.wallet.data.EnvironmentStore
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

@HiltViewModel
class EnvSelectViewModel @Inject constructor(
    private val environmentStore: EnvironmentStore
) : ViewModel() {
    data class State(
        val loading: Boolean,
        val availableEnvironments: List<Environment>,
        val selectedEnvironment: Environment?
    )

    private val state = environmentStore.observeSelectedEnvironment()
        .scan(State(loading = true, Environment.values().toList(), null)) { state, newEnv ->
            state.copy(loading = false, selectedEnvironment = newEnv)
        }
        .replay(1)
        .refCount()
        .distinctUntilChanged()

    fun observeState(): Observable<State> = state

    fun selectEnvironment(environment: Environment) {
        environmentStore.setSelectedEnvironment(environment)
    }
}