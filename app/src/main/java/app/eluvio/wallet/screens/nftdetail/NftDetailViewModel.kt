package app.eluvio.wallet.screens.nftdetail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.MediaSectionEntity
import app.eluvio.wallet.data.entities.NftEntity
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.data.stores.FulfillmentStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.screens.destinations.NftDetailDestination
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class NftDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contentStore: ContentStore,
    private val fulfillmentStore: FulfillmentStore,
    private val apiProvider: ApiProvider,
) : BaseViewModel<NftDetailViewModel.State>(State()) {
    @Immutable
    data class State(
        val title: String = "",
        val subtitle: String = "",
        val featuredMedia: List<MediaEntity> = emptyList(),
        val sections: List<MediaSectionEntity> = emptyList(),
        val redeemableOffers: List<Offer> = emptyList(),
        val backgroundImage: String? = null,
    ) {
        data class Offer(
            val offerId: String,
            val name: String,
            val imageUrl: String?,
            val contractAddress: String,
            val tokenId: String
        )
    }

    private val contractAddress = NftDetailDestination.argsFrom(savedStateHandle).contractAddress
    private val tokenId = NftDetailDestination.argsFrom(savedStateHandle).tokenId
    private var prefetchDisposable: Disposable? = null

    override fun onResume() {
        super.onResume()
        apiProvider.getFabricEndpoint()
            .flatMapPublisher { endpoint ->
                contentStore.observeNft(contractAddress, tokenId)
                    .doOnNext { prefetchNftInfoOnce(it) }
                    .map { it to endpoint }
            }
            .subscribeBy(
                onNext = { (nft, endpoint) ->
                    val bg =
                        nft.featuredMedia
                            .map { it.tvBackgroundImage }
                            .firstOrNull { it.isNotEmpty() }
                            ?.let { "$endpoint$it" }
                    // Theoretically we could keep looking for tvBackgroundImage in [mediaSections],
                    // but we don't need to for now (I think).
                    val offers = nft.redeemableOffers.map {
                        val imageUrl = (it.posterImagePath ?: it.imagePath)?.let { path ->
                            "${endpoint}${path}"
                        }
                        State.Offer(it.offerId, it.name, imageUrl, nft.contractAddress, nft.tokenId)
                    }
                    updateState {
                        State(
                            title = nft.displayName,
                            subtitle = nft.description,
                            featuredMedia = nft.featuredMedia,
                            sections = nft.mediaSections,
                            redeemableOffers = offers,
                            backgroundImage = bg,
                        )
                    }
                },
                onError = {

                })
            .addTo(disposables)
    }

    /**
     * Immediately returns the nft, but won't complete until nft info is fetched from network if nft as redeemable offers.
     */
    private fun prefetchNftInfoOnce(nft: NftEntity) {
        if (prefetchDisposable == null) {
            prefetchDisposable = fulfillmentStore.refreshRedeemedOffers(nft)
                .ignoreElement()
                .doOnError { Log.w("prefetch offer info failed. This could cause problems in viewing offer $it") }
                .retry(2)
                .subscribeBy(onError = {
                    Log.e("prefetched failed and not retrying!")
                })
                .addTo(disposables)
        }
    }
}
