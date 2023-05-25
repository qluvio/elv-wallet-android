package app.eluvio.wallet.ui.mediagallery

import app.eluvio.wallet.app.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

@HiltViewModel
class MediaGalleryViewModel @Inject constructor(
) : BaseViewModel<MediaGalleryViewModel.State>() {
    data class State(val f: Int)

    override val state: Observable<State> = Observable.just(State(1))
}