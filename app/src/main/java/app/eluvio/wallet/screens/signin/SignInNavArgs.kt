package app.eluvio.wallet.screens.signin

import app.eluvio.wallet.data.entities.v2.LoginProviders
import com.ramcosta.composedestinations.spec.Direction

/**
 * Shared nav args for all sign in modes. They don't all need all the data, but it makes it easier
 * to navigate between them.
 */
data class SignInNavArgs(
    val provider: LoginProviders = LoginProviders.UNKNOWN,
    val propertyId: String? = null,
    // Will clear data before navigating to the appropriate auth flow
    val signOutBeforeAuthFlow: Boolean = false,
    // Where the auth flow should navigate to once successfully signed in
    val onSignedInDirection: Direction? = null
)
