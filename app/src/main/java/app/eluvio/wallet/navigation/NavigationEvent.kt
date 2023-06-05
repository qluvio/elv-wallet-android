package app.eluvio.wallet.navigation

import androidx.navigation.NavOptions

sealed interface NavigationEvent {
    object GoBack : NavigationEvent
    data class Push(val destination: Screens, val navOptions: NavOptions? = null) : NavigationEvent

    //TODO: naming is hard
    data class ClearStackAndSetRoot(val root: Screens) : NavigationEvent
}

typealias NavigationCallback = (NavigationEvent) -> Unit

fun Screens.asPushDestination(navOptions: NavOptions? = null) =
    NavigationEvent.Push(this, navOptions)
