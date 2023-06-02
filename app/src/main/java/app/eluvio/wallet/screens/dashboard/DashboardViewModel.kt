package app.eluvio.wallet.screens.dashboard

import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.network.AuthServicesApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authServicesApi: AuthServicesApi
) : BaseViewModel<DashboardViewModel.State>(State()) {
    data class State(val f: Int = 0)
}
