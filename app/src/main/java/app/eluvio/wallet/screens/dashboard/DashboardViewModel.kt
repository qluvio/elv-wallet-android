package app.eluvio.wallet.screens.dashboard

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.stores.TokenStore
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tokenStore: TokenStore,
) : BaseViewModel<List<Tabs>>(
    if (tokenStore.isLoggedIn) {
        Tabs.AuthTabs
    } else {
        Tabs.NoAuthTabs
    }, savedStateHandle
) {
    override fun onResume() {
        super.onResume()

        tokenStore.loggedInObservable
            .subscribeBy { loggedIn ->
                if (loggedIn) {
                    updateState { Tabs.AuthTabs }
                } else {
                    updateState { Tabs.NoAuthTabs }
                }
            }
            .addTo(disposables)
    }
}
