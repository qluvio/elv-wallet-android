package app.eluvio.wallet.util.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.app.Events
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.util.logging.Log

// TODO: this name mention nothing about navigation/events/lifecycle being handled.. :/
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

    // Important to register AFTER we subscribed to state/nav/events, otherwise we might miss events emitted by VM
    vm.observeLifecycle(LocalLifecycleOwner.current.lifecycle)
}

@Composable
fun BaseViewModel<*>.observeLifecycle(lifecycle: Lifecycle) {
    DisposableEffect(lifecycle) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) = onResume()
            override fun onPause(owner: LifecycleOwner) = onPause()
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}
