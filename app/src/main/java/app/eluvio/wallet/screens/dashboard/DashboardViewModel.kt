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
) : BaseViewModel<DashboardViewModel.State>(
    State(isLoggedIn = tokenStore.isLoggedIn), savedStateHandle
) {
    data class State(
        val isLoggedIn: Boolean = false,
        // We'll probably only ever use these, because we no longer show tabs in unauthed state
        val tabs: List<Tabs> = Tabs.AuthTabs,
    )

    override fun onResume() {
        super.onResume()

        tokenStore.loggedInObservable
            .subscribeBy { loggedIn ->
                updateState { copy(isLoggedIn = loggedIn) }
            }
            .addTo(disposables)
    }
}
