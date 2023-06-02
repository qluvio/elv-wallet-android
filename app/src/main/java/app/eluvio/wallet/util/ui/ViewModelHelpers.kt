package app.eluvio.wallet.util.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rxjava3.subscribeAsState
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.util.logging.Log

@Composable
inline fun <reified VM : BaseViewModel<State>, State : Any> VM.subscribeToState(
    block: (VM, State) -> Unit
) {
    state.subscribeAsState(initial = null).value?.let { state ->
        block(this, state)
    }
}

@Composable
fun BaseViewModel<*>.handleNavigationEvents(navCallback: NavigationCallback) {
    DisposableEffect(Unit) {
        val navigationEvents = navigationEvents.subscribe {
            Log.d("${this.javaClass.simpleName} navigating to $it")
            navCallback(it)
        }
        onDispose { navigationEvents.dispose() }
    }
}
