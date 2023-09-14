package app.eluvio.wallet.screens.home

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import app.eluvio.wallet.navigation.PreLaunchGraph
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination

// TODO: home is a bad name, this is just the log that decides where to go when app is opened
@PreLaunchGraph(start = true)
@Destination(
    navArgsDelegate = DeeplinkArgs::class,
    // Handles direct deep links
    deepLinks = [
        DeepLink(
            uriPattern = "https://eluv.io/wallet#/wallet/users/me/items/ictr{contractId}/{tokenId}"
        ),
        DeepLink(
            uriPattern = "https://www.eluv.io/wallet#/wallet/users/me/items/ictr{contractId}/{tokenId}"
        )
    ]
)
@Composable
fun Home() {
    hiltViewModel<HomeViewModel>().subscribeToState { _, _ ->
        /*NoOp, just need to kick off the vm*/
    }
}
