package app.eluvio.wallet.screens.dashboard.mymedia

import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.stores.ContentStore
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class MyMediaViewModel @Inject constructor(
    private val contentStore: ContentStore
) : BaseViewModel<MyMediaViewModel.State>(State()) {
    data class State(val mediaItems: List<MediaEntity> = emptyList())

    override fun onResume() {
        super.onResume()
        contentStore.observeMediaItems()
            .map { list ->
                list.filter { isMediaPlayable(it) }
            }
            .subscribeBy(
                onNext = { next ->
                    updateState { copy(mediaItems = next) }
                },
                onError = { }
            )
            .addTo(disposables)
    }

    private fun isMediaPlayable(mediaEntity: MediaEntity): Boolean {
        return mediaEntity.mediaType in listOf(
            MediaEntity.MEDIA_TYPE_GALLERY,
            MediaEntity.MEDIA_TYPE_VIDEO
        )
    }
}
