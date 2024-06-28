package app.eluvio.wallet.screens.signin.preamble

import androidx.compose.runtime.Immutable
import app.eluvio.wallet.BuildConfig
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.SelectedEnvEntity.Environment
import app.eluvio.wallet.data.stores.EnvironmentStore
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import javax.inject.Inject

@HiltViewModel
class SignInPreambleViewModel @Inject constructor(
    private val environmentStore: EnvironmentStore
) : BaseViewModel<SignInPreambleViewModel.State>(State()) {
    @Immutable
    data class State(
        val loading: Boolean = true,
        val selectedEnvironment: Environment? = null,
        val availableEnvironments: List<Environment> = if (BuildConfig.DEBUG) {
            Environment.entries
        } else {
            listOf(Environment.Main)
        },
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
            .subscribe()
            .addTo(disposables)
    }
}
