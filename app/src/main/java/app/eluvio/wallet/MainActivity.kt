package app.eluvio.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import app.eluvio.wallet.navigation.Screen
import app.eluvio.wallet.screens.envselect.EnvSelect
import app.eluvio.wallet.screens.mediagallery.MediaGallery
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
            val navCallback: (Screen) -> Unit by rememberUpdatedState(newValue = {
                if (it == Screen.GoBack) {
                    navController.popBackStack()
                } else {
                    navController.navigate(it.route)
                }
            })
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
                            composable(Screen.Home.route) { Home(navCallback) }
                            composable(Screen.EnvironmentSelection.route) { EnvSelect(navCallback) }
                            composable(Screen.SignIn.route) { SignIn(navCallback) }
                            composable(Screen.MediaGallery.route) { MediaGallery(navCallback) }
                        }
                    }
                }
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
