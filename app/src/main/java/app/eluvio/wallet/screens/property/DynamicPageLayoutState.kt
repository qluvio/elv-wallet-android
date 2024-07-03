package app.eluvio.wallet.screens.property

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import androidx.media3.exoplayer.source.MediaSource
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.RedeemableOfferEntity
import app.eluvio.wallet.navigation.NavigationEvent

/**
 * Currently this is only used by PropertyPages, but we were planning to use it as the new
 * NFTDetail page, so we made it more generic so different ViewModels can provide dynamic layouts.
 */
@Immutable
data class DynamicPageLayoutState(
    /** Some images are provided as paths, use this as */
    val imagesBaseUrl: String? = null,

    val backgroundImagePath: String? = null,
    val rows: List<Row> = emptyList(),

    val searchNavigationEvent: NavigationEvent? = null,

    // For cross-app deeplinks
    val backLinkUrl: String? = null,
    val backButtonLogo: String? = null,
) {
    fun isEmpty() = rows.isEmpty() && backgroundImagePath == null

    /**
     * Returns the full URL for a given path, using [imagesBaseUrl] if available.
     * If [imagesBaseUrl] is not available, returns the path as-is.
     */
    fun urlForPath(path: String): String {
        return imagesBaseUrl?.let { "$it/$path" } ?: path
    }

    sealed interface Row {
        // TODO: maybe combine Title and Description into a single "Text" Row type,
        //  but then we'd have to start passing around predefined styles or something
        @Immutable
        data class Title(val text: AnnotatedString) : Row

        @Immutable
        data class Description(val text: AnnotatedString) : Row

        @Immutable
        data class Banner(val imagePath: String) : Row

        @Immutable
        data class Carousel(
            val title: String? = null,
            val subtitle: String? = null,
            val showAllNavigationEvent: NavigationEvent? = null,
            val items: List<CarouselItem>
        ) : Row
    }

    sealed interface CarouselItem {
        @Immutable
        data class Media(val entity: MediaEntity) : CarouselItem

        @Immutable
        data class SubpropertyLink(
            val subpropertyId: String,
            val imageUrl: String?,
        ) : CarouselItem

        @Immutable
        data class RedeemableOffer(
            val offerId: String,
            val name: String,
            val fulfillmentState: RedeemableOfferEntity.FulfillmentState,
            val contractAddress: String,
            val tokenId: String,
            val imageUrl: String?,
            val animation: MediaSource?,
        ) : CarouselItem
    }
}
