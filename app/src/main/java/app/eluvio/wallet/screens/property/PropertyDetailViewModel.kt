package app.eluvio.wallet.screens.property

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.app.Events
import app.eluvio.wallet.data.entities.v2.MediaPageEntity
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
import app.eluvio.wallet.data.entities.v2.SearchFiltersEntity
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

    override fun onResume() {
        super.onResume()

        apiProvider.getFabricEndpoint()
            .subscribeBy { updateState { copy(imagesBaseUrl = it) } }
            .addTo(disposables)

        val pageLayout = propertyStore.observeMediaProperty(propertyId)
            .switchMap { property ->
                val mainPage = property.mainPage ?: return@switchMap Flowable.empty()
                propertyStore.observeSections(property, mainPage)
                    .map { sections -> sections.associateBy { section -> section.id } }
                    .map { sections -> mainPage to sections }
            }
        val searchFilters = propertySearchStore.getFilters(propertyId)
            .onErrorReturnItem(SearchFiltersEntity())

        Flowable.combineLatest(pageLayout, searchFilters) { (page, sections), filters ->
            Triple(page, sections, filters)
        }
            .subscribeBy(
                onNext = { (mainPage, sections, filters) ->
                    updateState {
                        copy(
                            backgroundImagePath = mainPage.backgroundImagePath,
                            searchNavigationEvent = PropertySearchDestination(propertyId).asPush(),
                            sections = sections(mainPage, sections, filters)
                        )
                    }
                },
                onError = {
                    // Note: might be a problem with deeplinks
                    Log.e("Error loading property detail. Popping screen", it)
                    fireEvent(Events.NetworkError)
                    navigateTo(NavigationEvent.GoBack)
                }
            )
            .addTo(disposables)
    }

    private fun sections(
        mainPage: MediaPageEntity,
        sections: Map<String, MediaPageSectionEntity>,
        filters: SearchFiltersEntity
    ): List<DynamicPageLayoutState.Section> {
        // We can't just iterate over [sections] because the order of sections is important and it
        // is defined by the Page's sectionIds.
        return mainPage.sectionIds
            .mapNotNull { sections[it] }
            .filterNot { section ->
                section.isHidden
                    .also { if (it) Log.v("Hiding unauthorized section $section") }
            }
            .flatMap { section -> section.toDynamicSections(propertyId, filters) }
    }
}
