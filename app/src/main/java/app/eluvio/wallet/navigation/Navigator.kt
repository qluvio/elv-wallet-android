package app.eluvio.wallet.navigation

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController

typealias Navigator = (event: NavigationEvent) -> Unit

val LocalNavigator =
    staticCompositionLocalOf<Navigator> { error("No NavigationHandler provided") }

class ComposeNavigator(
    private val navController: NavController,
    private val onBackPressedDispatcherOwner: OnBackPressedDispatcherOwner
) : Navigator {
    override fun invoke(event: NavigationEvent) {
        when (event) {
            NavigationEvent.GoBack -> {
                if (!navController.popBackStack()) {
                    onBackPressedDispatcherOwner.onBackPressedDispatcher.onBackPressed()
                }
            }

            is NavigationEvent.Push -> {
                navController.navigate(event.direction.route, event.navOptions)
            }
        }
    }
}
