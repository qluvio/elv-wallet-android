package app.eluvio.wallet.screens.property.mediagrid

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.app.Events
import app.eluvio.wallet.data.permissions.PermissionResolver
import app.eluvio.wallet.data.entities.v2.DisplayFormat
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
import app.eluvio.wallet.data.entities.v2.display.SimpleDisplaySettings
import app.eluvio.wallet.data.permissions.PermissionContext
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.data.permissions.PermissionContextResolver
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.screens.destinations.MediaGridDestination
import app.eluvio.wallet.screens.property.DynamicPageLayoutState
import app.eluvio.wallet.screens.property.toCarouselItems
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class MediaGridViewModel @Inject constructor(
    private val permissionContextResolver: PermissionContextResolver,
    private val contentStore: ContentStore,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<MediaGridViewModel.State>(State(), savedStateHandle) {

    @Immutable
    data class State(
        val loading: Boolean = true,
        val title: String? = null,
        val items: List<DynamicPageLayoutState.CarouselItem> = emptyList()
    )

    private val permissionContext = MediaGridDestination.argsFrom(savedStateHandle)

    override fun onResume() {
        super.onResume()

        // When coming from Search, permissionContext.(sectionId/sectionItemId) will be set to some
        // arbitrary value from the API, that doesn't actually mean anything and won't be found in
        // the database. This should be fine, as pageId should be null in that case, which will
        // make [permissionContextResolver] skip the section resolution.
        permissionContextResolver
            .resolve(permissionContext)
            .switchMap { resolved ->
                if (permissionContext.mediaItemId != null) {
                    observeMediaItems(resolved)
                } else if (permissionContext.sectionId != null) {
                    getSectionItem(resolved.section)
                } else {
                    Flowable.error(IllegalStateException("Media grid launched without mediaItemId or sectionId."))
                }
            }
            .subscribeBy(
                onNext = { stateUpdate ->
                    updateState {
                        copy(
                            loading = false,
                            title = stateUpdate.title,
                            items = stateUpdate.items
                        )
                    }
                },
                onError = {
                    // Required fields are missing
                    Log.e("Error loading items", it)
                    fireEvent(Events.ToastMessage("Error loading items."))
                    navigateTo(NavigationEvent.GoBack)
                }
            )
            .addTo(disposables)
    }

    private fun getSectionItem(section: MediaPageSectionEntity?): Flowable<State> {
        if (section == null) {
            return Flowable.error(RuntimeException("Section not found"))
        }
        val items = section.items.toCarouselItems(
            permissionContext,
            SimpleDisplaySettings(displayFormat = DisplayFormat.GRID)
        )
        return Flowable.just(State(title = section.displaySettings?.title, items = items))
    }

    private fun observeMediaItems(resolved: PermissionContext.Resolved): Flowable<State> {
        val mediaContainer = resolved.mediaItem
        if (mediaContainer == null) {
            return Flowable.error(RuntimeException("Media container not found"))
        } else if (mediaContainer.mediaItemsIds.isEmpty()) {
            return Flowable.error(RuntimeException("Media container is empty"))
        }

        return contentStore.observeMediaItems(
            permissionContext.propertyId,
            mediaContainer.mediaItemsIds,
            forceRefresh = false
        )
            .doOnNext { mediaItems ->
                // Resolve permissions
                PermissionResolver.resolvePermissions(
                    mediaItems,
                    mediaContainer.resolvedPermissions,
                    resolved.property.permissionStates
                )
            }
            .map { mediaItems ->
                mediaItems
                    .filterNot { it.isHidden }
                    .map { mediaEntity ->
                        DynamicPageLayoutState.CarouselItem.Media(
                            // TODO: potential bug? we are losing info about the containing list/collection
                            permissionContext = permissionContext.copy(mediaItemId = mediaEntity.id),
                            entity = mediaEntity
                        )
                    }
            }
            .map { State(title = mediaContainer.name, items = it) }
    }
}
