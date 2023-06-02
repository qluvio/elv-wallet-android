package app.eluvio.wallet.screens.home

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.navigation.Screens
import app.eluvio.wallet.util.ui.handleNavigationEvents
import app.eluvio.wallet.util.ui.subscribeToState

// TODO: home is a bad name, this is just the log that decides where to go when app is opened
@Composable
fun Home(navCallback: NavigationCallback) {
    val vm = hiltViewModel<HomeViewModel>()
    vm.handleNavigationEvents(navCallback = NavigationCallback { screen, _ ->
        navCallback(screen) {
            popUpTo(Screens.Home.route) {
                inclusive = true
            }
        }
    })
    vm.subscribeToState { _, _ -> /*NoOp, just need to kick off the vm*/ }
}
