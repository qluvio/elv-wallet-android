package app.eluvio.wallet.screens.dashboard.myitems

import app.eluvio.wallet.data.entities.NftEntity
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.util.rx.mapNotNull
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject

/**
 * Extracted logic from MyItemsViewModel to share with other ViewModels that need the same data.
 */
class AllMediaProvider @Inject constructor(
    private val contentStore: ContentStore
) {
    data class State(
        val loading: Boolean = true,
        val media: List<Media> = emptyList(),
    ) {
        data class Media(
            val key: String,
            val contractAddress: String,
            val imageUrl: String,
            val title: String,
            val subtitle: String? = null,
            // if there's only one token, we can show the token id
            val tokenId: String? = null,
            val tokenCount: Int = 1,
        )
    }

    //TODO: this is a HACK. The ContentStore needs to emit information about whether the
    // list is empty because we haven't populated our cache yet and are still fetching from network,
    // or it's truly empty.
    // The hacks checks if this is the second time the store has responded with an empty list,
    // so the second one probably means the api actually returned an empty list.
    private var storeEmissions = 0

    fun observeAllMedia(onNetworkError: () -> Unit): Flowable<State> {
        return contentStore.observeWalletData()
            .doOnNext {
                if (it.isFailure) {
                    onNetworkError()
                }
            }
            .mapNotNull { it.getOrNull() }
            .map { response -> response.map { nft -> nft.toMediaState() } }
            .map { media ->
                storeEmissions++
                val loading = storeEmissions < 2 && media.isEmpty()
                State(loading, media)
            }
    }

    private fun NftEntity.toMediaState() = State.Media(
        key = _id,
        contractAddress = contractAddress,
        imageUrl = imageUrl,
        title = displayName,
        subtitle = editionName,
        // If there's only one token, we can show the token id
        tokenId = tokenId,
        tokenCount = 1
    )
}
