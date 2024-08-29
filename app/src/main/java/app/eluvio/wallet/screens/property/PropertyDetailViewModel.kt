package app.eluvio.wallet.screens.property

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.app.Events
import app.eluvio.wallet.data.entities.v2.MediaPageEntity
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
import app.eluvio.wallet.data.entities.v2.MediaPropertyEntity
import app.eluvio.wallet.data.entities.v2.SearchFiltersEntity
import app.eluvio.wallet.data.entities.v2.permissions.PermissionContext
import app.eluvio.wallet.data.entities.v2.permissions.PermissionsEntity
import app.eluvio.wallet.data.entities.v2.permissions.showAlternatePage
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.data.stores.PropertySearchStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.destinations.PropertyDetailDestination
import app.eluvio.wallet.screens.destinations.PropertySearchDestination
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class PropertyDetailViewModel @Inject constructor(
    private val propertyStore: MediaPropertyStore,
    private val propertySearchStore: PropertySearchStore,
    private val apiProvider: ApiProvider,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<DynamicPageLayoutState>(DynamicPageLayoutState()) {

    private val propertyId = PropertyDetailDestination.argsFrom(savedStateHandle).propertyId

    private val unauthorizedPageIds = mutableSetOf<String>()

    override fun onResume() {
        super.onResume()

        apiProvider.getFabricEndpoint()
            .subscribeBy { updateState { copy(imagesBaseUrl = it) } }
            .addTo(disposables)

        val pageLayout = propertyStore.observeMediaProperty(propertyId)
            .switchMap { property ->
                unauthorizedPageIds.clear()
                getFirstAuthorizedPage(property, null)
                    .map { page -> property to page }
            }
            .switchMap { (property, page) ->
                propertyStore.observeSections(property, page)
                    .map { sections -> sections.associateBy { section -> section.id } }
                    .map { sections -> page to sections }
            }
        val searchFilters = propertySearchStore.getFilters(propertyId)
            .onErrorReturnItem(SearchFiltersEntity())

        Flowable.combineLatest(pageLayout, searchFilters) { (page, sections), filters ->
            Triple(page, sections, filters)
        }
            .subscribeBy(
                onNext = { (page, sections, filters) ->
                    updateState {
                        copy(
                            backgroundImagePath = page.backgroundImagePath,
                            searchNavigationEvent = PropertySearchDestination(propertyId).asPush(),
                            sections = sections(page, sections, filters)
                        )
                    }
                },
                onError = { exception ->
                    if (exception === CircularRedirectException) {
                        Log.e("Circular redirect detected")
                        fireEvent(Events.ToastMessage("Permission error. Unable to load page."))
                        navigateTo(NavigationEvent.GoBack)
                    } else {
                        // Note: might be a problem with deeplinks
                        Log.e("Error loading property detail. Popping screen", exception)
                        fireEvent(Events.NetworkError)
                        navigateTo(NavigationEvent.GoBack)
                    }
                }
            )
            .addTo(disposables)
    }

    private fun getFirstAuthorizedPage(
        property: MediaPropertyEntity,
        currentPage: MediaPageEntity?
    ): Flowable<MediaPageEntity> {
        // Convenience function to handle redirects
        fun redirect(redirectPageId: String) = propertyStore.observePage(property, redirectPageId)
            .switchMap { nextPage ->
                // Recursively check the next page
                getFirstAuthorizedPage(property, nextPage)
            }

        return if (currentPage == null) {
            // Check property permissions
            property.propertyPermissions
                ?.getRedirectPageId()
                ?.let { redirect(it) }
            // We're authorized to view the property, check the main page.
                ?: getFirstAuthorizedPage(property, property.mainPage)
        } else {
            when (val redirectPageId = currentPage.pagePermissions?.getRedirectPageId()) {
                currentPage.id, in unauthorizedPageIds -> {
                    // We already checked this page id, or this is a self-reference, so we know
                    // we're not authorized to view it.
                    Flowable.error(CircularRedirectException)
                }

                null -> {
                    // No page to redirect to: we are authorized to render this page.
                    Flowable.just(currentPage)
                        .doOnNext { Log.i("Authorized to view page ${currentPage.id}") }
                }

                else -> {
                    Log.w("Reached unauthorized page ${currentPage.id}, redirecting to $redirectPageId")
                    unauthorizedPageIds += currentPage.id
                    propertyStore.observePage(property, redirectPageId)
                        .switchMap { nextPage ->
                            // Recursively check the next page
                            getFirstAuthorizedPage(property, nextPage)
                        }
                }
            }
        }
    }

    /**
     * If we are authorized to view this page/property, or redirect behavior isn't configured, returns null.
     */
    private fun PermissionsEntity.getRedirectPageId(): String? {
        return alternatePageId?.takeIf { showAlternatePage }
    }

    private fun sections(
        page: MediaPageEntity,
        sections: Map<String, MediaPageSectionEntity>,
        filters: SearchFiltersEntity
    ): List<DynamicPageLayoutState.Section> {
        val pagePermissionContext = PermissionContext(
            propertyId = propertyId,
            pageId = page.id
        )
        // We can't just iterate over [sections] because the order of sections is important and it
        // is defined by the Page's sectionIds.
        return page.sectionIds
            .mapNotNull { sections[it] }
            .filterNot { section ->
                section.isHidden
                    .also { if (it) Log.v("Hiding unauthorized section $section") }
            }
            .flatMap { section ->
                section.toDynamicSections(pagePermissionContext, filters)
            }
    }
}

private val CircularRedirectException = IllegalStateException("Circular redirect detected")
