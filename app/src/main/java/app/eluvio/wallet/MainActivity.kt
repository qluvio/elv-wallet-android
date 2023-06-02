package app.eluvio.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import app.eluvio.wallet.navigation.NavigationCallback
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

            val navCallback: NavigationCallback = remember {
                NavigationCallback { screen, navOptionsBuilder ->
                    if (screen == Screens.GoBack) {
                        if (!navController.popBackStack()) {
                            onBackPressedDispatcher.onBackPressed()
                        }
                    } else {
                        val options = navOptionsBuilder?.let { navOptions(it) }
                        navController.navigate(screen.route, options)
                    }
                }
            }
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
                            startDestination = "/app/home"
                        ) {
                            composable(Screens.Home.route) { Home(navCallback) }
                            composable(Screens.EnvironmentSelection.route) { EnvSelect(navCallback) }
                            composable(Screens.SignIn.route) { SignIn(navCallback) }
                            composable(Screens.Dashboard.route) { Dashboard(navCallback) }
                        }
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
