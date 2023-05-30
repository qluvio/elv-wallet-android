package app.eluvio.wallet.ui.mediagallery

import app.eluvio.wallet.app.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MediaGalleryViewModel @Inject constructor(
) : BaseViewModel<MediaGalleryViewModel.State>(State()) {
    data class State(val f: Int = 0)
}
