package app.eluvio.wallet.screens.signin

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.SignOutHandler
import app.eluvio.wallet.data.entities.v2.LoginProviders
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.navigation.asReplace
import app.eluvio.wallet.screens.destinations.Auth0SignInDestination
import app.eluvio.wallet.screens.destinations.OrySignInDestination
import app.eluvio.wallet.screens.navArgs
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class SignInRouterViewModel @Inject constructor(
    private val signOutHandler: SignOutHandler,
    private val propertyStore: MediaPropertyStore,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<Unit>(Unit, savedStateHandle) {

    private val navArgs = savedStateHandle.navArgs<SignInNavArgs>()

    override fun onResume() {
        super.onResume()

        val preAuthAction =
            if (navArgs.signOutBeforeAuthFlow) {
                signOut()
            } else {
                Completable.complete()
                    .doOnSubscribe { Log.d("Pre-auth signout not required.") }
            }

        preAuthAction
            .subscribeBy {
                when (navArgs.provider) {
                    LoginProviders.AUTH0 -> Auth0SignInDestination(navArgs)
                    LoginProviders.ORY -> OrySignInDestination(navArgs)
                }.let { navigateTo(it.asReplace()) }
            }
            .addTo(disposables)
    }

    /**
     * Sign out without restarting the app. And attempt to fetch required data for the auth flow.
     */
    private fun signOut() = signOutHandler.signOut(restartAppOnComplete = false)
        .doOnSubscribe { Log.d("Starting pre-auth sign-out") }
        .doOnComplete { Log.d("Pre-Auth signOut complete") }
        // Consume errors
        .doOnError { Log.e("Pre-auth signout failed. Ignoring.", it) }
        .onErrorComplete()
        .andThen(
            // There's some potential overkill here, since we only really need to fetch the property
            // defined in navArgs.propertyId, but fetching a specific property is an authenticated
            // API call and we already lost our token.
            // Another hidden bug - once we support pagination, if the property we need isn't in the
            // first page of /mw/properties, we are fetching for nothing, and the next page will
            // have problems displaying the right data.
            propertyStore.fetchMediaProperties()
                .doOnSubscribe { Log.d("Starting to fetch Properties with no token.") }
                .doOnComplete { Log.d("Pre-Auth property fetch complete.") }
        )
}
