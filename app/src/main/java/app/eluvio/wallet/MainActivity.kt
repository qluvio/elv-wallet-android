package app.eluvio.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.navigation.Screens
import app.eluvio.wallet.screens.dashboard.Dashboard
import app.eluvio.wallet.screens.envselect.EnvSelect
import app.eluvio.wallet.screens.home.Home
import app.eluvio.wallet.screens.signin.SignIn
import app.eluvio.wallet.theme.EluvioTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            val navCallback: NavigationCallback = remember { NavigationHandler(navController) }
            EluvioTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundBrush)
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurface
                    ) {
                        NavHost(
                            navController,
                            startDestination = Screens.Home.route,
                            builder = navGraph(navCallback)
                        )
                    }
                }
            }
        }
    }

    private fun navGraph(navCallback: NavigationCallback): (NavGraphBuilder.() -> Unit) = {
        // This is more code than just writing it out line by line, but this way we are forced at compile time to handle new screens.
        Screens.values().forEach { screen ->
            composable(screen.route) {
                when (screen) {
                    Screens.Home -> Home(navCallback)
                    Screens.EnvironmentSelection -> EnvSelect(navCallback)
                    Screens.SignIn -> SignIn(navCallback)
                    Screens.Dashboard -> Dashboard(navCallback)
                }
            }
        }
    }

    private inner class NavigationHandler(private val navController: NavController) :
        NavigationCallback {
        override fun invoke(event: NavigationEvent) {
            when (event) {
                NavigationEvent.GoBack -> {
                    if (!navController.popBackStack()) {
                        onBackPressedDispatcher.onBackPressed()
                    }
                }

                is NavigationEvent.Push -> {
                    navController.navigate(event.destination.route, event.navOptions)
                }

                is NavigationEvent.ClearStackAndSetRoot -> {
                    navController.navigate(event.root.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            }
        }

    }
}

private val backgroundBrush: ShaderBrush = object : ShaderBrush() {
    override fun createShader(size: Size): Shader {
        return RadialGradientShader(
            colors = listOf(Color(0xff202020), Color.Black),
            center = size.toRect().topCenter,
            radius = size.width / 4f,
            colorStops = listOf(0f, 0.95f)
        )
    }
}
