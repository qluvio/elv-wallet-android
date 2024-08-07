package app.eluvio.wallet.screens.property.search

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.MediaEntity.Companion.ASPECT_RATIO_WIDE
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
import app.eluvio.wallet.screens.property.toDynamicSections
import app.eluvio.wallet.util.Toaster
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.rx.Optional
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
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
    private val toaster: Toaster,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<PropertySearchViewModel.State>(State(), savedStateHandle) {

    @Immutable
    data class State(
        val loading: Boolean = true,
        val loadingResults: Boolean = false,
        val baseUrl: String? = null,
        val headerLogo: String? = null,
        val propertyName: String? = null,

        val primaryFilters: DynamicPageLayoutState.Section? = null,
        val searchResults: List<DynamicPageLayoutState.Section> = emptyList(),
        val selectedFilters: SelectedFilters? = null,
    ) {
        val allSections = listOfNotNull(primaryFilters) + searchResults

        /**
         * Represents the currently selected filters.
         * All instances represent a non-empty selection of at least Primary filter and value.
         */
        data class SelectedFilters(
            val primaryFilterAttribute: SearchFiltersEntity.Attribute,
            val primaryFilterValue: String,
            /**
             * The secondary filter matching the selected primary filter value.
             * The existence of this field doesn't mean that a secondary filter is selected yet.
             */
            val secondaryFilterAttribute: SearchFiltersEntity.Attribute?,
            /**
             * When this field is non-null, the secondary filter is actually selected.
             */
            val secondaryFilterValue: String? = null,
        )
    }

    private val navArgs = savedStateHandle.navArgs<PropertySearchNavArgs>()

    private val query = BehaviorProcessor.createDefault(QueryUpdate("", true))
    private val manualSearch = PublishProcessor.create<Unit>()

    private val selectedPrimaryFilter =
        BehaviorProcessor.createDefault(Optional.empty<State.SelectedFilters>())

    override fun onResume() {
        super.onResume()

        selectedPrimaryFilter
            .subscribeBy {
                updateState { copy(selectedFilters = it.orDefault(null)) }
            }
            .addTo(disposables)

        apiProvider.getFabricEndpoint()
            .subscribeBy {
                updateState { copy(baseUrl = it) }
            }
            .addTo(disposables)

        propertyStore.observeMediaProperty(navArgs.propertyId)
            .subscribeBy {
                updateState {
                    copy(
                        headerLogo = it.headerLogo,
                        propertyName = it.name
                    )
                }
            }
            .addTo(disposables)


        searchStore.getFilters(navArgs.propertyId)
            .firstOrError(
                // We don't actually want to observe changes, because in the rare case that filters
                // change WHILE we're already showing them, the user can get into a weird state.
            )
            .subscribeBy(
                onSuccess = { filters ->
                    updateState {
                        copy(
                            // Technically we might not have finished loading the Property at this point,
                            // but we still know the filters, so we can show them.
                            loading = false,
                            primaryFilters = filters.primaryFilter?.toCustomCardsSection(baseUrl)
                        )
                    }

                    // Now that everything is ready, we can start observing search triggers.
                    observeSearchTriggers(filters)
                },
                onError = {
                    toaster.toast("We hit a problem. Please try again later.")
                    navigateTo(NavigationEvent.GoBack)
                }
            )
            .addTo(disposables)
    }

    fun onQueryChanged(query: String) {
        this.query.onNext(QueryUpdate(query, immediate = false))
    }

    fun onBackPressed() {
        val selectedFilter = selectedPrimaryFilter.value?.orDefault(null)
        when {
            query.value?.query?.isNotEmpty() == true -> {
                fireEvent(ResetQueryEvent)
                query.onNext(QueryUpdate("", immediate = true))
            }

            selectedFilter != null -> {
                if (selectedFilter.secondaryFilterValue != null) {
                    // Secondary filter is selected, clear it
                    val updatedFilter = selectedFilter.copy(secondaryFilterValue = null)
                    selectedPrimaryFilter.onNext(Optional.of(updatedFilter))
                } else {
                    // Only primary filter is selected, clear it.
                    selectedPrimaryFilter.onNext(Optional.empty())
                }
            }
            // Using [GoBack] sends us into an infinite loop, so instead we assume we
            // know the current destination, and pop it.
            else -> navigateTo(NavigationEvent.PopTo(PropertySearchDestination, true))
        }
    }

    fun onPrimaryFilterSelected(primaryFilterValue: SearchFiltersEntity.AttributeValue?) {
        val selectedFilter = primaryFilterValue?.let {
            val primaryFilterAttribute = primaryFilterValue.attribute()
            val nextFilter = primaryFilterAttribute.searchFilters().attributes
                .find { it.id == primaryFilterValue.nextFilterAttribute }
            State.SelectedFilters(
                primaryFilterAttribute = primaryFilterAttribute,
                primaryFilterValue = primaryFilterValue.value,
                secondaryFilterAttribute = nextFilter,
            )
        }
        selectedPrimaryFilter.onNext(Optional.of(selectedFilter))
    }

    fun onSecondaryFilterClicked(value: String?) {
        val selectedFilter = selectedPrimaryFilter.value?.orDefault(null)
            ?: return Log.w("Secondary filter selected without primary filter?!")
        val updatedFilter = selectedFilter.copy(
            secondaryFilterValue = value
            // If clicked the currently selected filter, deselect it.
            // Disabled for now, since we switched to select by highlighting.
            // .takeIf { it != selectedFilter.secondaryFilterValue }
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

    private fun observeSearchTriggers(searchFilters: SearchFiltersEntity) {
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
            .doOnNext {
                val primaryFilterSelected = it.orDefault(null)?.primaryFilterValue != null
                updateState {
                    copy(primaryFilters = searchFilters.primaryFilter
                        // only show primary filters while non are selected
                        ?.takeIf { !primaryFilterSelected }
                        ?.toCustomCardsSection(baseUrl))
                }
            }
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
            .switchMapSingle { request -> fetchResults(request) }
            .subscribeBy { results ->
                updateState {
                    // If filters aren't defined for this tenant, always show results as a grid
                    val forceGrid = primaryFilters == null
                    val sections = results.flatMap { section ->
                        section.toDynamicSections(
                            navArgs.propertyId,
                            // Never show ViewAll button for search results
                            viewAllThreshold = Int.MAX_VALUE,
                            forceGridView = forceGrid
                        )
                    }.ifEmpty {
                        // Show a message when no results are found
                        listOf(
                            DynamicPageLayoutState.Section.Title(
                                "no_results",
                                AnnotatedString("No results found")
                            )
                        )
                    }
                    copy(searchResults = sections)
                }
            }
            .addTo(disposables)
    }

    private fun SearchFiltersEntity.Attribute.toCustomCardsSection(imageBaseUrl: String?): DynamicPageLayoutState.Section {
        return DynamicPageLayoutState.Section.Carousel(
            sectionId = "PRIMARY_FILTERS",
            items = values.map { filterValue ->
                DynamicPageLayoutState.CarouselItem.CustomCard(
                    imageUrl = filterValue.image?.let { imagePath -> "$imageBaseUrl${imagePath}" },
                    title = filterValue.value,
                    aspectRatio = ASPECT_RATIO_WIDE,
                    onClick = { onPrimaryFilterSelected(filterValue) }
                )
            },
            showAsGrid = true
        )
    }
}

private data class QueryUpdate(val query: String, val immediate: Boolean)

private sealed interface SearchTriggers {
    data class QueryChanged(val query: String) : SearchTriggers

    data class FilterChanged(
        val filter: PropertySearchViewModel.State.SelectedFilters?
    ) : SearchTriggers {
        fun toAttributeMap(): Map<String, List<String>>? = filter?.run {
            buildMap {
                put(primaryFilterAttribute.id, listOf(primaryFilterValue))
                if (secondaryFilterAttribute != null && secondaryFilterValue != null) {
                    put(
                        secondaryFilterAttribute.id,
                        listOf(secondaryFilterValue)
                    )
                }
            }
        }
    }
}
