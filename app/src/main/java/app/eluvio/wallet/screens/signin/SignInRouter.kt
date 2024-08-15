package app.eluvio.wallet.screens.signin

import androidx.compose.runtime.Composable
import app.eluvio.wallet.data.entities.v2.LoginProviders
import app.eluvio.wallet.navigation.AuthFlowGraph
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.asReplace
import app.eluvio.wallet.screens.destinations.Auth0SignInDestination
import app.eluvio.wallet.screens.destinations.OrySignInDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.spec.Direction

@AuthFlowGraph(start = true)
@Destination
@Composable
fun SignInRouter(
    provider: LoginProviders = LoginProviders.UNKNOWN,
    propertyId: String? = null,
    afterAuthDestination: Direction? = null,
) {
    val navigator = LocalNavigator.current
    when (provider) {
        // Default to Auth0
        LoginProviders.UNKNOWN,
        LoginProviders.AUTH0 -> navigator(Auth0SignInDestination(propertyId).asReplace())

        LoginProviders.ORY -> navigator(OrySignInDestination(propertyId).asReplace())
    }
}
