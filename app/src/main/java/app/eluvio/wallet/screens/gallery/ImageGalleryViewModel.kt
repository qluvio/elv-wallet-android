package app.eluvio.wallet.screens.gallery

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.screens.destinations.ImageGalleryDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class ImageGalleryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contentStore: ContentStore,
) : BaseViewModel<ImageGalleryViewModel.State>(State()) {
    data class State(val images: List<GalleryImage> = emptyList()) {
        data class GalleryImage(val url: String, val name: String)
    }

    private val mediaEntityId = ImageGalleryDestination.argsFrom(savedStateHandle).mediaEntityId
    override fun onResume() {
        super.onResume()
        contentStore.observeMediaItem(mediaEntityId)
            .map { media ->
                when (media.mediaType) {
                    MediaEntity.MEDIA_TYPE_GALLERY -> {
                        media.gallery?.mapNotNull { galleryItem ->
                            galleryItem.imageUrl?.let { url ->
                                State.GalleryImage(url, galleryItem.name)
                            }
                        } ?: emptyList()
                    }

                    MediaEntity.MEDIA_TYPE_IMAGE -> {
                        val image = media.mediaFile.takeIf { it.isNotEmpty() }
                            ?.let { State.GalleryImage(it, media.name) }
                        listOfNotNull(image)
                    }

                    else -> emptyList()
                }
            }
            .subscribeBy(
                onNext = {
                    //todo throw if list is empty?
                    updateState { State(images = it) }
                },
                onError = {

                }
            )
            .addTo(disposables)
    }
}
