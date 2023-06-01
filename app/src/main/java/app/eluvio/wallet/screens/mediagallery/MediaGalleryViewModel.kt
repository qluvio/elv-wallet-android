package app.eluvio.wallet.screens.mediagallery

import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.network.AuthServicesApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MediaGalleryViewModel @Inject constructor(
    private val authServicesApi: AuthServicesApi
) : BaseViewModel<MediaGalleryViewModel.State>(State()) {
    data class State(val f: Int = 0)
}
