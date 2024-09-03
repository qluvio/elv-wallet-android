package app.eluvio.wallet.screens.signin

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import app.eluvio.wallet.navigation.AuthFlowGraph
import app.eluvio.wallet.screens.common.DelayedFullscreenLoader
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@AuthFlowGraph(start = true)
@Destination(navArgsDelegate = SignInNavArgs::class)
@Composable
fun SignInRouter() {
    hiltViewModel<SignInRouterViewModel>().subscribeToState { _, _ ->
        // ViewModel will handle everything, just show a spinner until it's done
        DelayedFullscreenLoader()
    }
}
