package app.eluvio.wallet.screens.property

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import androidx.media3.exoplayer.source.MediaSource
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.RedeemableOfferEntity
import app.eluvio.wallet.data.entities.v2.SearchFiltersEntity
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
    val sections: List<Section> = emptyList(),

    val searchNavigationEvent: NavigationEvent? = null,

    // For cross-app deeplinks
    val backLinkUrl: String? = null,
    val backButtonLogo: String? = null,

    val captureTopFocus: Boolean = true,
) {
    fun isEmpty() = sections.isEmpty() && backgroundImagePath == null

    /**
     * Returns the full URL for a given path, using [imagesBaseUrl] if available.
     * If [imagesBaseUrl] is not available, returns the path as-is.
     */
    fun urlForPath(path: String): String {
        return imagesBaseUrl?.let { "$it$path" } ?: path
    }

    sealed interface Section {
        // TODO: maybe combine Title and Description into a single "Text" Row type,
        //  but then we'd have to start passing around predefined styles or something
        @Immutable
        data class Title(val text: AnnotatedString) : Section

        @Immutable
        data class Description(val text: AnnotatedString) : Section

        @Immutable
        data class Banner(val imagePath: String) : Section

        @Immutable
        data class Carousel(
            val title: String? = null,
            val subtitle: String? = null,
            val viewAllNavigationEvent: NavigationEvent? = null,
            val items: List<CarouselItem>,
            // Whether to show this as a row or a grid
            val showAsGrid: Boolean = false,
            val filterAttribute: SearchFiltersEntity.Attribute? = null,
        ) : Section
    }

    sealed interface CarouselItem {
        @Immutable
        data class Media(val entity: MediaEntity, val propertyId: String) : CarouselItem

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
