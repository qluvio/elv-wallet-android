package app.eluvio.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.screens.NavGraphs
import app.eluvio.wallet.theme.EluvioTheme
import app.eluvio.wallet.util.logging.Log
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EluvioTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundBrush)
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurface
                    ) {
                        val navController = rememberNavController()
                        val navCallback = remember { NavigationHandler(navController) }
                        DestinationsNavHost(
                            navGraph = NavGraphs.root,
                            navController = navController,
                            dependenciesContainerBuilder = {
                                dependency(navCallback)
                            }
                        )
                        // Print nav backstack for debugging
                        navController.currentBackStack.collectAsState().value.print()
                    }
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
                    navController.navigate(event.direction.route, event.navOptions)
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

private fun Collection<NavBackStackEntry>.print(prefix: String = "stack") {
    val stack = map { it.destination.route }.toTypedArray().contentToString()
    Log.v("$prefix = $stack")
}
