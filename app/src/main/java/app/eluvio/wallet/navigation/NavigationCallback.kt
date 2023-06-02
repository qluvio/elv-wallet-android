package app.eluvio.wallet.navigation

import androidx.navigation.NavOptionsBuilder

/**
 * This is all just fancy kotlin magic to get NavigationCallback to behave like:
 * [typealias NavigationCallback = (Screen, NavOptionsBuilder.() -> Unit) -> Unit]
 * but also make it easy to invoke without providing the NavOptionsBuilder part.
 */
interface NavigationCallback {
    operator fun invoke(screen: Screen, options: (NavOptionsBuilder.() -> Unit)? = null)

    companion object {
        operator fun invoke(block: (Screen, (NavOptionsBuilder.() -> Unit)?) -> Unit): NavigationCallback {
            return object : NavigationCallback {
                override fun invoke(screen: Screen, options: (NavOptionsBuilder.() -> Unit)?) {
                    block(screen, options)
                }
            }
        }

        val NoOp = NavigationCallback { _, _ -> }
    }
}
