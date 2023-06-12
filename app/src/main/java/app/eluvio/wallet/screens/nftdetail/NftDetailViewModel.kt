package app.eluvio.wallet.screens.nftdetail

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.screens.destinations.NftDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NftDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<NftDetailViewModel.State>(State()) {
    data class State(val id: String = "")

    private val nftId = NftDetailDestination.argsFrom(savedStateHandle).nftId

    override fun onResume() {
        super.onResume()
        updateState { copy(id = nftId) }
    }
}
