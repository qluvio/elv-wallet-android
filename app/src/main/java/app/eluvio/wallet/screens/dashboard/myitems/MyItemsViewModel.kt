package app.eluvio.wallet.screens.dashboard.myitems

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.app.Events
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.rx.Optional
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.combineLatest
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.processors.BehaviorProcessor
import javax.inject.Inject

@HiltViewModel
class MyItemsViewModel @Inject constructor(
    private val allMediaProvider: AllMediaProvider,
    private val propertyStore: MediaPropertyStore,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<MyItemsViewModel.State>(State(), savedStateHandle) {

    @Immutable
    data class State(
        val allMedia: AllMediaProvider.State = AllMediaProvider.State(),
        val properties: List<PropertyInfo> = emptyList(),
        val selectedProperty: PropertyInfo? = null,
    ) {
        @Immutable
        data class PropertyInfo(
            val name: String,
            val id: String,
        )
    }

    private val selectedProperty =
        BehaviorProcessor.createDefault(Optional.empty<State.PropertyInfo>())

    override fun onResume() {
        super.onResume()

        allMediaProvider.observeAllMedia(onNetworkError = { fireEvent(Events.NetworkError) })
            .combineLatest(selectedProperty.distinctUntilChanged())
            .subscribeBy(
                onNext = { (allMedia, selectedProperty) ->
                    val filterProperty = selectedProperty.orDefault(null)
                    val media = if (filterProperty != null) {
                        // TODO: swap for API lookup, instead of local filtering
                        allMedia.media.filter { it.propertyId == filterProperty.id }
                    } else {
                        allMedia.media
                    }
                    updateState {
                        copy(
                            allMedia = allMedia.copy(media = media),
                            selectedProperty = filterProperty
                        )
                    }
                },
                onError = { Log.e("Error getting wallet data", it) }
            )
            .addTo(disposables)

        propertyStore.observeOwnedProperties()
            .map {
                it.properties.map { (id, name) -> State.PropertyInfo(id = id, name = name) }
            }
            .subscribeBy(
                onNext = { properties -> updateState { copy(properties = properties) } },
                onError = { Log.e("Error observing owned properties", it) }
            )
            .addTo(disposables)
    }

    fun onPropertySelected(propertyInfo: State.PropertyInfo?) {
        val nextValue = propertyInfo?.takeIf { selectedProperty.value?.orDefault(null) != it }
        selectedProperty.onNext(Optional.of(nextValue))
    }
}
