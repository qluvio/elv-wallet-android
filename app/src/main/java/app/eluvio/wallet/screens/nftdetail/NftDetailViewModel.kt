package app.eluvio.wallet.screens.nftdetail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.media3.exoplayer.source.MediaSource
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.VideoOptionsFetcher
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.MediaSectionEntity
import app.eluvio.wallet.data.entities.NftEntity
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.data.stores.FulfillmentStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.screens.destinations.NftDetailDestination
import app.eluvio.wallet.screens.videoplayer.toMediaSource
import app.eluvio.wallet.util.logging.Log
import com.google.common.base.Optional
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
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
    private val videoOptionsFetcher: VideoOptionsFetcher,
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
            val contractAddress: String,
            val tokenId: String,
            val imageUrl: String?,
            val animation: MediaSource?,
        )
    }

    private val contractAddress = NftDetailDestination.argsFrom(savedStateHandle).contractAddress
    private val tokenId = NftDetailDestination.argsFrom(savedStateHandle).tokenId
    private var prefetchDisposable: Disposable? = null
    private var offerLoaderDisposable: Disposable? = null

    override fun onResume() {
        super.onResume()
        apiProvider.getFabricEndpoint()
            .flatMapPublisher { endpoint ->
                contentStore.observeNft(contractAddress, tokenId)
                    .doOnNext { prefetchNftInfoOnce(it) }
                    .map { it to endpoint }
            }
            .doOnNext { (nft, endpoint) ->
                loadOffers(nft, endpoint)
            }
            .subscribeBy(
                onNext = { (nft, endpoint) ->
                    // Theoretically we could keep looking for tvBackgroundImage in [mediaSections],
                    // but we don't need to for now (I think).
                    val backgroundImage =
                        nft.featuredMedia
                            .map { it.tvBackgroundImage }
                            .firstOrNull { it.isNotEmpty() }
                            ?.let { "$endpoint$it" }
                    updateState {
                        copy(
                            title = nft.displayName,
                            subtitle = nft.description,
                            featuredMedia = nft.featuredMedia,
                            sections = nft.mediaSections,
                            backgroundImage = backgroundImage,
                        )
                    }
                },
                onError = {

                })
            .addTo(disposables)
    }

    /**
     * Loads offers with animations and images. Updates the current state once completed.
     */
    private fun loadOffers(nft: NftEntity, endpoint: String) {
        // Offers we don't have redeemState for aren't confirmed to valid to show to the user
        // TODO: also filter out expired/inactive offers?
        val validOfferIds = nft.redeemStates.map { it.offerId }.toSet()
        val offerStates = nft.redeemableOffers
            .filter { validOfferIds.contains(it.offerId) }
            .map { offer ->
                val animationPath = offer.animation.values.firstOrNull()
                val videoOptions: Single<Optional<MediaSource>> = if (animationPath == null) {
                    Single.just(Optional.absent())
                } else {
                    videoOptionsFetcher.fetchVideoOptionsFromPath(animationPath)
                        .map { videoEntity -> Optional.of(videoEntity.toMediaSource()) }
                        .onErrorReturnItem(Optional.absent())
                }
                videoOptions.map { optional ->
                    val imageUrl = (offer.posterImagePath ?: offer.imagePath)?.let { path ->
                        "${endpoint}${path}"
                    }
                    State.Offer(
                        offer.offerId,
                        offer.name,
                        nft.contractAddress,
                        nft.tokenId,
                        // Loaded later
                        imageUrl,
                        animation = optional.orNull()
                    )
                }
            }
        offerLoaderDisposable?.dispose()
        offerLoaderDisposable = if (offerStates.isEmpty()) {
            null
        } else {
            Single.zip(offerStates) { array -> array.filterIsInstance<State.Offer>() }
                .subscribeBy(
                    onSuccess = { offers ->
                        updateState { copy(redeemableOffers = offers) }
                    },
                    onError = {
                        Log.e("stav: fuck ", it)
                    }
                )
                .addTo(disposables)
        }
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
