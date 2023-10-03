package app.eluvio.wallet.screens.home

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.AuthenticationService
import app.eluvio.wallet.data.stores.DeeplinkStore
import app.eluvio.wallet.data.stores.TokenStore
import app.eluvio.wallet.navigation.asNewRoot
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.NavGraphs
import app.eluvio.wallet.screens.destinations.HomeDestination
import app.eluvio.wallet.screens.destinations.SkuDetailsDestination
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tokenStore: TokenStore,
    private val deeplinkStore: DeeplinkStore,
    private val authenticationService: AuthenticationService,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<HomeViewModel.State>(State()) {
    data class State(
        // Loading state isn't shown by default, since usually this screen is only shown for a
        // moment while figuring out where to go next.
        val showLoading: Boolean = false,
    )

    private val navArgs = HomeDestination.argsFrom(savedStateHandle)
    override fun onResume() {
        super.onResume()
        Log.e("Started Home with args: $navArgs")

        val deepLink = navArgs.toDeeplinkRequest()
            ?.also { deeplinkStore.deeplinkRequest = it }
            ?: deeplinkStore.deeplinkRequest
        //TODO, if there's another token but we didn't get all the way to fabricToken, we might want to pick up mid-flow
        if (deepLink != null) {
            handleDeeplink(deepLink)
        } else if (tokenStore.fabricToken == null) {
            Log.w("User not signed in, navigating to auth flow")
            navigateTo(NavGraphs.authFlowGraph.asNewRoot())
        } else {
            Log.w("User signed in, navigating to dashboard")
            navigateTo(NavGraphs.mainGraph.asNewRoot())
        }
    }

    private fun handleDeeplink(deepLink: DeeplinkStore.DeeplinkRequest) {
        // consume deeplink
        deeplinkStore.deeplinkRequest = null

        // We need to authenticate with the deeplink JWT, this could take a moment, so show a loading state in the meanwhile.
        updateState { State(showLoading = true) }

        tokenStore.wipe()
        tokenStore.idToken = deepLink.jwt

        authenticationService.getFabricTokenExternal()
            .subscribeBy(
                onSuccess = {
                    Log.d("Successfully got fabric token from deeplink jwt: $it")
                    navigateTo(NavGraphs.mainGraph.asNewRoot())
                    navigateTo(
                        SkuDetailsDestination(
                            marketplace = deepLink.marketplace,
                            sku = deepLink.sku
                        ).asPush()
                    )
                },
                onError = {
                    Log.e("Failed to get fabric token", it)
                    // We failed to get a fabric token, so we need to re-authenticate.
                    navigateTo(NavGraphs.authFlowGraph.asNewRoot())
                }
            )
            .addTo(disposables)
    }
}
