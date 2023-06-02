package app.eluvio.wallet.screens.dashboard

import app.eluvio.wallet.app.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    // Maybe we don't need a view model for this screen?
) : BaseViewModel<DashboardViewModel.State>(State()) {
    data class State(val f: Int = 0)
}
