package app.eluvio.wallet.screens.dashboard.discover

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.v2.MediaPropertyEntity
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.di.ApiProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val propertyStore: MediaPropertyStore,
    private val apiProvider: ApiProvider,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<DiscoverViewModel.State>(State(), savedStateHandle) {

    @Immutable
    data class State(
        val loading: Boolean = true,
        val properties: List<MediaPropertyEntity> = emptyList(),
        val baseUrl: String = "",
    )

    override fun onResume() {
        super.onResume()

        apiProvider.getFabricEndpoint()
            .subscribeBy { updateState { copy(baseUrl = it) } }
            .addTo(disposables)

        propertyStore.observeMediaProperties(true)
            .map { properties ->
                // We can't handle properties without images
                properties.filter { property -> property.image.isNotEmpty() }
            }
            .subscribeBy(
                onNext = { properties ->
                    // Assume that Properties will never be empty once fetched from Server
                    updateState { copy(properties = properties, loading = properties.isEmpty()) }
                },
                onError = {}
            )
            .addTo(disposables)
    }
}
