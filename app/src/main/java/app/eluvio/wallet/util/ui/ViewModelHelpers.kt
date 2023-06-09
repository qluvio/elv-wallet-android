package app.eluvio.wallet.util.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rxjava3.subscribeAsState
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.app.Events
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.util.logging.Log

// TODO: this name mention nothing about navigation being handled.. :/
@Composable
inline fun <reified VM : BaseViewModel<State>, State : Any> VM.subscribeToState(
    noinline navCallback: NavigationCallback,
    crossinline onEvent: (Events) -> Unit = {},
    onState: (VM, State) -> Unit
) {
    val vm = this

    // Handle navigation events
    DisposableEffect(Unit) {
        val navigationEvents = vm.navigationEvents.subscribe {
            Log.d("${vm.javaClass.simpleName} navigating to $it")
            navCallback(it)
        }
        val events = vm.events.subscribe {
            Log.d("${vm.javaClass.simpleName} event fired: $it")
            onEvent(it)
        }
        onDispose {
            navigationEvents.dispose()
            events.dispose()
        }
    }

    vm.state.subscribeAsState(initial = null).value?.let { state ->
        onState(vm, state)
    }
}
