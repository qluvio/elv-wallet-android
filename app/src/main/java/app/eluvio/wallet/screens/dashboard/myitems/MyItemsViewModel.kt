package app.eluvio.wallet.screens.dashboard.myitems

import app.eluvio.wallet.app.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyItemsViewModel @Inject constructor(
) : BaseViewModel<MyItemsViewModel.State>(State()) {
    data class State(
        val media: List<Media> = (1..100).map { Media() }, // should really be emptyList() by default
    ) {
        data class Media(
            val img: String = "https://demov3.net955210.contentfabric.io/s/demov3/q/hq__9j2PTimhyfpDGz4Jq6H8aRRoAjp4TQaudRCa4ncZ59djzkp39bZsxwhf8fb67b95BH9JexdnJw/files/meridian_poster_square-crop.jpeg",
            val title: String = "Meridian",
        )
    }
}
