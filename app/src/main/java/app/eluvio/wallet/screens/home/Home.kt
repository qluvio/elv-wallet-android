package app.eluvio.wallet.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import app.eluvio.wallet.navigation.PreLaunchGraph
import app.eluvio.wallet.screens.common.EluvioLoadingSpinner
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
            uriPattern = "elvwallet://items/{marketplace}/{contract}/{sku}?jwt={jwt}&entitlement={entitlement}",
        ),
    ]
)
@Composable
fun Home() {
    hiltViewModel<HomeViewModel>().subscribeToState { _, state ->
        if (state.showLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                EluvioLoadingSpinner()
            }
        }
    }
}
