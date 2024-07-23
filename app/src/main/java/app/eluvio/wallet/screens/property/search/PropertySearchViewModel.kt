package app.eluvio.wallet.screens.property.search

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
import app.eluvio.wallet.data.entities.v2.SearchFiltersEntity
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.data.stores.PropertySearchStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.network.dto.v2.SearchRequest
import app.eluvio.wallet.screens.destinations.PropertySearchDestination
import app.eluvio.wallet.screens.navArgs
import app.eluvio.wallet.screens.property.DynamicPageLayoutState
import app.eluvio.wallet.screens.property.toCarousel
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.rx.Optional
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.combineLatest
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.processors.PublishProcessor
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class PropertySearchViewModel @Inject constructor(
    private val propertyStore: MediaPropertyStore,
    private val searchStore: PropertySearchStore,
    private val apiProvider: ApiProvider,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<PropertySearchViewModel.State>(State(), savedStateHandle) {

    @Immutable
    data class State(
        val loading: Boolean = true,
        val loadingResults: Boolean = false,
        val baseUrl: String? = null,
        val headerLogo: String? = null,
        val propertyName: String? = null,

        // Primary filters will be displayed before search results
        val primaryFilter: SearchFiltersEntity.Attribute? = null,
        val searchResults: DynamicPageLayoutState = DynamicPageLayoutState(captureTopFocus = false),
        val selectedFilters: SelectedFilters? = null,
    ) {
        data class SelectedFilters(
            val primaryFilterAttribute: SearchFiltersEntity.Attribute,
            val primaryFilterValue: String,
            val secondaryFilterAttribute: SearchFiltersEntity.Attribute?,
            val secondaryFilterValue: String? = null,
        )
    }

    private val navArgs = savedStateHandle.navArgs<PropertySearchNavArgs>()

    private val query = BehaviorProcessor.createDefault(QueryUpdate("", true))
    private val manualSearch = PublishProcessor.create<Unit>()

    private val selectedPrimaryFilter =
        BehaviorProcessor.createDefault(Optional.empty<State.SelectedFilters>())

    private var searchFilters: SearchFiltersEntity? = null

    override fun onResume() {
        super.onResume()

        selectedPrimaryFilter
            .subscribeBy {
                updateState { copy(selectedFilters = it.orDefault(null)) }
            }
            .addTo(disposables)

        observeSearchTriggers()

        apiProvider.getFabricEndpoint()
            .subscribeBy {
                updateState {
                    copy(
                        baseUrl = it,
                        searchResults = searchResults.copy(imagesBaseUrl = it)
                    )
                }
            }
            .addTo(disposables)

        propertyStore.observeMediaProperty(navArgs.propertyId)
            .combineLatest(searchStore.getFilters(navArgs.propertyId))
            .subscribeBy { (property, filters) ->
                searchFilters = filters
                updateState {
                    copy(
                        loading = false,
                        headerLogo = property.headerLogo,
                        propertyName = property.name,
                        primaryFilter = searchFilters?.primaryFilter,
                    )
                }
            }
            .addTo(disposables)
    }

    fun onQueryChanged(query: String) {
        this.query.onNext(QueryUpdate(query, immediate = false))
    }

    fun onBackPressed() {
        when {
            query.value?.query?.isNotEmpty() == true -> {
                fireEvent(ResetQueryEvent)
                query.onNext(QueryUpdate("", immediate = true))
            }

            selectedPrimaryFilter.value?.isPresent == true -> {
                selectedPrimaryFilter.onNext(Optional.empty())
            }
            // Using [GoBack] sends us into an infinite loop, so instead we assume we
            // know the current destination, and pop it.
            else -> navigateTo(NavigationEvent.PopTo(PropertySearchDestination, true))
        }
    }

    fun onPrimaryFilterSelected(primaryFilterValue: SearchFiltersEntity.AttributeValue?) {
        val primaryFilterAttribute = searchFilters?.primaryFilter
        val selectedFilter = if (primaryFilterAttribute != null && primaryFilterValue != null) {
            val nextFilter = searchFilters?.attributes
                ?.find { it.id == primaryFilterValue.nextFilterAttribute }
            State.SelectedFilters(
                primaryFilterAttribute = primaryFilterAttribute,
                primaryFilterValue = primaryFilterValue.value,
                secondaryFilterAttribute = nextFilter,
            )
        } else null
        selectedPrimaryFilter.onNext(Optional.of(selectedFilter))
    }

    fun onSecondaryFilterClicked(value: String) {
        val selectedFilter = selectedPrimaryFilter.value?.orDefault(null)
            ?: return Log.w("Secondary filter selected without primary filter?!")
        val updatedFilter = selectedFilter.copy(
            secondaryFilterValue = value
                // If clicked the currently selected filter, deselect it.
                .takeIf { it != selectedFilter.secondaryFilterValue }
        )
        selectedPrimaryFilter.onNext(Optional.of(updatedFilter))
    }

    fun onSearchClicked() {
        manualSearch.onNext(Unit)
    }

    private fun fetchResults(request: SearchRequest): Single<List<MediaPageSectionEntity>> {
        return searchStore.search(navArgs.propertyId, request)
            .doOnSubscribe {
                updateState { copy(loadingResults = true) }
                Log.d("Starting to search for $request")
            }
            .doFinally {
                updateState { copy(loadingResults = false) }
                Log.d("Done searching for $request")
            }
    }

    private fun observeSearchTriggers() {
        val queryChanged = query
            .switchMapSingle { (query, immediate) ->
                if (immediate) {
                    Single.just(query)
                } else {
                    Single.just(query)
                        .delay(300, TimeUnit.MILLISECONDS)
                }
            }
            .map { SearchTriggers.QueryChanged(it) }
        val searchClicked = manualSearch
            .map { SearchTriggers.QueryChanged(query.value?.query ?: "") }
        val filterChanged = selectedPrimaryFilter
            .map { SearchTriggers.FilterChanged(it.orDefault(null)) }
            .distinctUntilChanged()

        Flowable.merge(
            queryChanged,
            searchClicked,
            filterChanged
        )
            .scan(SearchRequest()) { request, trigger ->
                when (trigger) {
                    is SearchTriggers.QueryChanged -> request.copy(searchTerm = trigger.query)
                    is SearchTriggers.FilterChanged -> {
                        request.copy(attributes = trigger.toAttributeMap())
                    }
                }
            }
            .distinctUntilChanged()
            .switchMapMaybe { request ->
                // Don't run empty searches before primary filter is selected
                val runSearch =
                    request.searchTerm?.isNotEmpty() == true || request.attributes != null
                if (runSearch) {
                    // Don't show primary filters anymore, we got a search term
                    fetchResults(request).toMaybe()
                        .doOnSubscribe {
                            updateState { copy(primaryFilter = null) }
                        }
                } else {
                    // When search term is emptied, but no filter is selected, show primary filters again
                    Maybe.never<List<MediaPageSectionEntity>>()
                        .doOnSubscribe {
                            updateState { copy(primaryFilter = searchFilters?.primaryFilter) }
                        }
                }
            }
            .subscribeBy { results ->
                val sections = results.map { section ->
                    section.toCarousel(
                        navArgs.propertyId,
                        // Never show ViewAll button for search results
                        viewAllThreshold = Int.MAX_VALUE
                    )
                }.ifEmpty {
                    // Show a message when no results are found
                    listOf(DynamicPageLayoutState.Section.Title(AnnotatedString("No results found")))
                }
                updateState {
                    copy(searchResults = searchResults.copy(sections = sections))
                }
            }
            .addTo(disposables)
    }

}

private data class QueryUpdate(val query: String, val immediate: Boolean)

private sealed interface SearchTriggers {
    data class QueryChanged(val query: String) : SearchTriggers

    data class FilterChanged(
        val filter: PropertySearchViewModel.State.SelectedFilters?
    ) : SearchTriggers {
         fun toAttributeMap(): Map<String, List<String>>? {
            filter ?: return null
            return buildMap {
                put(filter.primaryFilterAttribute.id, listOfNotNull(filter.primaryFilterValue))
                if (filter.secondaryFilterAttribute != null && filter.secondaryFilterValue != null) {
                    put(
                        filter.secondaryFilterAttribute.id,
                        listOfNotNull(filter.secondaryFilterValue)
                    )
                }
            }
        }
    }
}

