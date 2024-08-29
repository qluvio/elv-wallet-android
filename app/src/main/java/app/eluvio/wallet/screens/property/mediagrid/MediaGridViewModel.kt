package app.eluvio.wallet.screens.property.mediagrid

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.screens.destinations.MediaGridDestination
import app.eluvio.wallet.screens.property.DynamicPageLayoutState
import app.eluvio.wallet.data.entities.v2.permissions.PermissionContext
import app.eluvio.wallet.screens.property.toCarouselItems
import app.eluvio.wallet.util.Toaster
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class MediaGridViewModel @Inject constructor(
    private val propertyStore: MediaPropertyStore,
    private val contentStore: ContentStore,
    private val toaster: Toaster,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<MediaGridViewModel.State>(State(), savedStateHandle) {

    @Immutable
    data class State(
        val loading: Boolean = true,
        val title: String? = null,
        val items: List<DynamicPageLayoutState.CarouselItem> = emptyList()
    )

    private val navArgs = MediaGridDestination.argsFrom(savedStateHandle)
    private val propertyId = navArgs.propertyId

    override fun onResume() {
        super.onResume()

        when {
            navArgs.sectionId != null -> loadSectionItems(navArgs.sectionId)
            navArgs.mediaContainerId != null -> loadMediaItems(navArgs.mediaContainerId)
            else -> {
                Log.e("MediaGrid launched with no Section or MediaList ID.")
                toaster.toast("Error loading items.")
                navigateTo(NavigationEvent.GoBack)
            }
        }
    }

    private fun loadMediaItems(mediaListId: String) {
        // Assume that if the media list is accessible, all the media items are accessible, so
        // we don't need to include section/sectionitem ids in the permission context.
        val permissionContext = PermissionContext(propertyId = propertyId)

        contentStore.observeMediaItem(mediaListId, propertyId)
            .switchMap { mediaList -> // Technically could be a MediaCollection or MediaList
                contentStore.observeMediaItems(propertyId, mediaList.mediaItemsIds)
                    .map { mediaItems -> mediaList to mediaItems }
            }
            .subscribeBy { (mediaList, mediaItems) ->
                updateState {
                    copy(
                        loading = false,
                        title = mediaList.name,
                        items = mediaItems.map { mediaEntity ->
                            DynamicPageLayoutState.CarouselItem.Media(
                                permissionContext = permissionContext,
                                entity = mediaEntity
                            )
                        }
                    )
                }
            }
            .addTo(disposables)
    }

    private fun loadSectionItems(sectionId: String) {
        val permissionContext = PermissionContext(propertyId = propertyId, sectionId = sectionId)
        propertyStore.observeSection(sectionId)
            .subscribe { section ->
                updateState {
                    copy(
                        loading = false,
                        title = section.title,
                        items = section.items.toCarouselItems(permissionContext)
                    )
                }
            }
            .addTo(disposables)
    }
}
