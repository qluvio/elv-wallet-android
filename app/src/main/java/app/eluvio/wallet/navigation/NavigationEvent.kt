package app.eluvio.wallet.navigation

import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import app.eluvio.wallet.screens.NavGraphs
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.spec.Direction

sealed interface NavigationEvent {
    object GoBack : NavigationEvent
    data class Push(
        val direction: Direction, val navOptions: NavOptions? = null
    ) : NavigationEvent
}

fun Direction.asPush() =
    NavigationEvent.Push(this)

fun Direction.asNewRoot() = NavigationEvent.Push(this, navOptions { popUpTo(NavGraphs.root) })
