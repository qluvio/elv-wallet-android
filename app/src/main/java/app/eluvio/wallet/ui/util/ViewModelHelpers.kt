package app.eluvio.wallet.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rxjava3.subscribeAsState
import app.eluvio.wallet.app.BaseViewModel

@Composable
inline fun <reified VM : BaseViewModel<State>, State : Any> VM.subscribeToState(
    block: (VM, State) -> Unit
){
    observeState().subscribeAsState(initial = null).value?.let{state->
        block(this, state)
    }
}
