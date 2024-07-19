package app.eluvio.wallet.screens.property.search

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.data.stores.PropertySearchStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.screens.navArgs
import app.eluvio.wallet.screens.property.DynamicPageLayoutState
import app.eluvio.wallet.screens.property.toCarousel
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.processors.BehaviorProcessor
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
        val searchResults: DynamicPageLayoutState = DynamicPageLayoutState(captureTopFocus = false),
        val onQueryChanged: (String) -> Unit = {},
        val onSearchClicked: () -> Unit = {},
    )

    private val navArgs = savedStateHandle.navArgs<PropertySearchNavArgs>()
    private val query = BehaviorProcessor.create<String>()
    private val manualSearch = BehaviorProcessor.create<String>()

    override fun onResume() {
        super.onResume()

        updateState {
            copy(
                onQueryChanged = { query.onNext(it) },
                onSearchClicked = { manualSearch.onNext(query.value ?: "") },
            )
        }

        observeQuery()

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
            .subscribeBy { property ->
                updateState {
                    copy(
                        loading = false,
                        headerLogo = property.headerLogo,
                        propertyName = property.name
                    )
                }
            }
            .addTo(disposables)
    }

    private fun observeQuery() {
        query
            .debounce(300, TimeUnit.MILLISECONDS)
            // Emit manual search immediately
            .mergeWith(manualSearch)
            .distinctUntilChanged()
            .switchMapSingle { fetchResults(it) }
            .subscribeBy { results ->
                Log.w("stav: actually search: $results")
                val sections = results.map { section ->
                    section.toCarousel(
                        navArgs.propertyId,
                        // Never show ViewAll button for search results
                        viewAllThreshold = Int.MAX_VALUE
                    )
                }
                updateState {
                    copy(searchResults = searchResults.copy(sections = sections))
                }
            }
            .addTo(disposables)
    }

    private fun fetchResults(query: String): Single<List<MediaPageSectionEntity>> {
        return searchStore.search(navArgs.propertyId, query)
            .doOnSubscribe { updateState { copy(loadingResults = true) } }
            .doFinally { updateState { copy(loadingResults = false) } }
    }
}
