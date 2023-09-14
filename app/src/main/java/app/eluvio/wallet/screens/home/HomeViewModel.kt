package app.eluvio.wallet.screens.home

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.stores.DeeplinkStore
import app.eluvio.wallet.data.stores.TokenStore
import app.eluvio.wallet.navigation.asNewRoot
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.NavGraphs
import app.eluvio.wallet.screens.destinations.HomeDestination
import app.eluvio.wallet.screens.destinations.NftDetailDestination
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tokenStore: TokenStore,
    private val deeplinkStore: DeeplinkStore,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<Unit>(Unit) {
    val navArgs = HomeDestination.argsFrom(savedStateHandle)
    override fun onResume() {
        super.onResume()
        val deepLink = if (navArgs.contractId != null && navArgs.tokenId != null) {
            DeeplinkStore.DeeplinkRequest(navArgs.contractId, navArgs.tokenId).also {
                deeplinkStore.deeplinkRequest = it
            }
        } else {
            deeplinkStore.deeplinkRequest
        }
        //TODO, if there's another token but we didn't get all the way to fabricToken, we might want to pick up mid-flow
        if (tokenStore.fabricToken == null) {
            Log.w("User not signed in, navigating to environment selection")
            navigateTo(NavGraphs.authFlowGraph.asNewRoot())
        } else if (deepLink != null) {
            // consume deeplink
            deeplinkStore.deeplinkRequest = null
            navigateTo(NavGraphs.mainGraph.asNewRoot())
            navigateTo(
                NftDetailDestination(deepLink.contractAddress, deepLink.tokenId).asPush()
            )
        } else {
            Log.w("User signed in, navigating to dashboard")
            navigateTo(NavGraphs.mainGraph.asNewRoot())
        }
    }
}
