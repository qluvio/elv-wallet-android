package app.eluvio.wallet.screens.dashboard.mymedia

import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.MediaSectionEntity
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.util.rx.mapNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class MyMediaViewModel @Inject constructor(
    private val contentStore: ContentStore
) : BaseViewModel<MyMediaViewModel.State>(State()) {
    data class State(
        val featuredMedia: List<MediaEntity> = emptyList(),
        val mediaSections: List<MediaSectionEntity> = emptyList()
    )

    override fun onResume() {
        super.onResume()

        contentStore.observeWalletData()
            .mapNotNull { it.getOrNull() }
            .map { nfts ->
                val featuredMedia = nfts.flatMap { it.featuredMedia }.distinct()
                val mediaSections = nfts.flatMap { it.mediaSections }.distinct()
                State(featuredMedia, mediaSections)
            }
            .subscribeBy(
                onNext = { newState -> updateState { newState } },
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
