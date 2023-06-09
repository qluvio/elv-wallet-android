package app.eluvio.wallet.screens.home

import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.stores.TokenStore
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.navigation.Screens
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tokenStore: TokenStore
) : BaseViewModel<Unit>(Unit) {
    override fun onStart() {
        super.onStart()
        //TODO, if there's another token but we didn't get all the way to fabricToken, we might want to pick up mid-flow
        if (tokenStore.fabricToken == null) {
            Log.w("User not signed in, navigating to environment selection")
            navigateTo(NavigationEvent.ClearStackAndSetRoot(Screens.EnvironmentSelection))
        } else {
            Log.w("User signed in, navigating to dashboard")
            navigateTo(NavigationEvent.ClearStackAndSetRoot(Screens.Dashboard))
        }
    }
}
