package app.eluvio.wallet.screens.dashboard.myitems

import android.os.Parcelable
import app.eluvio.wallet.data.entities.NftTemplateEntity
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.util.rx.mapNotNull
import io.reactivex.rxjava3.core.Flowable
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

/**
 * Extracted logic from MyItemsViewModel to share with other ViewModels that need the same data.
 */
class AllMediaProvider @Inject constructor(
    private val contentStore: ContentStore
) {
    @Parcelize
    data class State(
        val loading: Boolean = true,
        val media: List<Media> = emptyList(),
    ) : Parcelable

    @Parcelize
    data class Media(
        val key: String,
        val contractAddress: String,
        val imageUrl: String,
        val title: String,
        val subtitle: String? = null,
        // Doesn't show up in cards, but is used in the detail view.
        val description: String,
        // if there's only one token, we can show the token id
        val tokenId: String? = null,
        val tokenCount: Int = 1,
        val tenant: String? = null
    ) : Parcelable {
        companion object {
            fun fromTemplate(
                nftTemplateEntity: NftTemplateEntity,
                imageOverride: String? = null,
                tokenId: String? = null,
                tokenCount: Int = 1
            ) = Media(
                key = nftTemplateEntity.id,
                contractAddress = nftTemplateEntity.contractAddress,
                imageUrl = imageOverride ?: nftTemplateEntity.imageUrl ?: "",
                title = nftTemplateEntity.displayName,
                subtitle = nftTemplateEntity.editionName,
                description = nftTemplateEntity.description,
                // If there's only one token, we can show the token id
                tokenId = tokenId,
                // TODO: design hasn't settled on how to treat token count, this will always be 1 for now
                tokenCount = tokenCount,
                tenant = nftTemplateEntity.tenant
            )
        }
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
            .map { response ->
                response.mapNotNull { nft ->
                    nft.nftTemplate?.let { template ->
                        Media.fromTemplate(template, nft.imageUrl, nft.tokenId)
                    }
                }
            }
            .map { media ->
                storeEmissions++
                val loading = storeEmissions < 2 && media.isEmpty()
                State(loading, media)
            }
    }
}
