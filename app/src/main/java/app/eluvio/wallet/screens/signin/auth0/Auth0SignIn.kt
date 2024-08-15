package app.eluvio.wallet.screens.signin.auth0

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import app.eluvio.wallet.navigation.AuthFlowGraph
import app.eluvio.wallet.screens.signin.common.SignInView
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@AuthFlowGraph
@Destination(navArgsDelegate = Auth0SignInNavArgs::class)
@Composable
fun Auth0SignIn() {
    hiltViewModel<Auth0SignInViewModel>().subscribeToState { vm, state ->
        SignInView(state, onRequestNewToken = vm::requestNewToken, showMetamaskLink = true)
    }
}
