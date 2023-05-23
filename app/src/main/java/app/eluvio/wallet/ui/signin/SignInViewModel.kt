package app.eluvio.wallet.ui.signin

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import app.eluvio.wallet.data.EnvironmentStore
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val environmentStore: EnvironmentStore
) : ViewModel() {
    data class State(@StringRes val env: Int)

    private val state = environmentStore.observeSelectedEnvironment()
        .map { State(it.envName) }
        .replay(1)
        .refCount()
        .distinctUntilChanged()

    fun observeState(): Observable<State> = state
}