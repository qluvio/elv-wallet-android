package app.eluvio.wallet

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.util.Consumer
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.rememberNavController
import app.eluvio.wallet.navigation.ComposeNavigator
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.screens.NavGraphs
import app.eluvio.wallet.theme.EluvioTheme
import app.eluvio.wallet.util.logging.Log
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EluvioTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
//                        .background(Color(0xFF050505))
                ) {
                    Image(
                        painterResource(id = R.drawable.bg_gradient),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                    val navController = rememberNavController()
                    DisposableEffect(navController) {
                        val consumer = Consumer<Intent> {
                            Log.d("New intent captured and forwarded to navController: $it")
                            navController.handleDeepLink(it)
                        }
                        this@MainActivity.addOnNewIntentListener(consumer)
                        onDispose {
                            this@MainActivity.removeOnNewIntentListener(consumer)
                        }
                    }
                    val navigator = remember {
                        ComposeNavigator(
                            navController,
                            onBackPressedDispatcherOwner = this@MainActivity
                        )
                    }
                    CompositionLocalProvider(
                        LocalNavigator provides navigator
                    ) {
                        DestinationsNavHost(
                            navGraph = NavGraphs.root,
                            navController = navController,
                        )
                        // Print nav backstack for debugging
                        navController.currentBackStack.collectAsState().value.print()
                    }
                }
            }
        }
    }
}

private fun Collection<NavBackStackEntry>.print(prefix: String = "navstack") {
    fun NavBackStackEntry.routeWithArgs(): String {
        val fallback = destination.route ?: ""
        return arguments?.keySet()?.fold(fallback) { route, key ->
            @Suppress("DEPRECATION")
            val value = arguments?.get(key)?.takeIf { it is String }?.toString() ?: "{$key}"
            route.replace("{$key}", value)
        } ?: fallback
    }

    val stack = map { it.routeWithArgs() }.toTypedArray().contentToString()
    Log.v("$prefix = $stack")
}
