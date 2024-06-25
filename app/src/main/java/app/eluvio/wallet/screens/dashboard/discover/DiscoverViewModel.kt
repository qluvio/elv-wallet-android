package app.eluvio.wallet.screens.dashboard.discover

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.AfterSignInDestination
import app.eluvio.wallet.data.entities.v2.MediaPropertyEntity
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.data.stores.TokenStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.NavGraphs
import app.eluvio.wallet.screens.destinations.PropertyDetailDestination
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val propertyStore: MediaPropertyStore,
    private val apiProvider: ApiProvider,
    private val tokenStore: TokenStore,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<DiscoverViewModel.State>(State(), savedStateHandle) {
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
            .subscribeBy(
                onNext = { properties ->
                    // Assume that Properties will never be empty once fetched from Server
                    updateState { copy(properties = properties, loading = properties.isEmpty()) }
                },
                onError = {}
            )
            .addTo(disposables)
    }

    fun onPropertyClicked(property: MediaPropertyEntity) {
        val destination = PropertyDetailDestination(property.id)
        if (tokenStore.isLoggedIn) {
            navigateTo(destination.asPush())
        } else {
            Log.d("User not signed in, navigating to authFlow and saving propertyId: ${property.id}")
            AfterSignInDestination.direction.set(destination)
            navigateTo(NavGraphs.authFlowGraph.asPush())
        }
    }
}
