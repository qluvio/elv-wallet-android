package app.eluvio.wallet.screens.property.search

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.screens.destinations.PropertySearchDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PropertySearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel<PropertySearchViewModel.State>(State(), savedStateHandle) {
    data class State(val tmp: Int = 0)

    private val navArgs = PropertySearchDestination.argsFrom(savedStateHandle)

    override fun onResume() {
        super.onResume()

    }
}
