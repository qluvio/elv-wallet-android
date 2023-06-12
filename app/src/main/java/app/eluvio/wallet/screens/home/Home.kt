package app.eluvio.wallet.screens.home

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.navigation.PreLaunchGraph
import app.eluvio.wallet.util.ui.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

// TODO: home is a bad name, this is just the log that decides where to go when app is opened
@PreLaunchGraph(start = true)
@Destination
@Composable
fun Home(navCallback: NavigationCallback) {
    hiltViewModel<HomeViewModel>().subscribeToState(navCallback) { _, _ ->
        /*NoOp, just need to kick off the vm*/
    }
}
