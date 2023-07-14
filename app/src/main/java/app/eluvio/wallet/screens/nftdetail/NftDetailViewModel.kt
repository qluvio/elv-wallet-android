package app.eluvio.wallet.screens.nftdetail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.MediaSectionEntity
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.screens.destinations.NftDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class NftDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contentStore: ContentStore,
    private val apiProvider: ApiProvider,
) : BaseViewModel<NftDetailViewModel.State>(State()) {
    @Immutable
    data class State(
        val title: String = "",
        val subtitle: String = "",
        val sections: List<MediaSectionEntity> = emptyList(),
        val backgroundImage: String? = null,
    )

    private val contractAddress = NftDetailDestination.argsFrom(savedStateHandle).contractAddress

    override fun onResume() {
        super.onResume()
        apiProvider.getFabricEndpoint()
            .flatMapPublisher { endpoint ->
                contentStore.observeNft(contractAddress)
                    .map { it to endpoint }
            }
            .subscribeBy(
                onNext = { (nfts, endpoint) ->
                    val nft = nfts.first()
                    val bg =
                        nft.mediaSections.firstOrNull()?.collections?.firstOrNull()?.media?.firstOrNull()?.tvBackgroundImage?.let {
                            "$endpoint$it"
                        }
                    updateState {
                        State(
                            title = nft.displayName,
                            subtitle = nft.description,
                            sections = nft.mediaSections,
                            backgroundImage = bg,
                        )
                    }
                },
                onError = {

                })
            .addTo(disposables)
    }
}
