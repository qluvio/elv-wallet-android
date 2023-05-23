package app.eluvio.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.eluvio.wallet.navigation.Screen
import app.eluvio.wallet.ui.envselect.EnvSelect
import app.eluvio.wallet.ui.signin.SignIn
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val navCallback: (Screen) -> Unit by rememberUpdatedState(newValue = {
                navController.navigate(it.route)
            })
            NavHost(navController, startDestination = "/app/home") {
                composable(Screen.Home.route) { Home(navCallback) }
                composable(Screen.EnvironmentSelection.route) { EnvSelect(navCallback) }
                composable(Screen.SignIn.route){ SignIn(navCallback) }
            }
        }
    }
}

@Composable
fun Home(onNavigation: (Screen) -> Unit) {
    //if signed in
    // wallet screen
    //else
    onNavigation(Screen.EnvironmentSelection)
}