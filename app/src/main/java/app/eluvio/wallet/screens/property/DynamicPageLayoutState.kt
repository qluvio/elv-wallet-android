package app.eluvio.wallet.screens.property

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.media3.exoplayer.source.MediaSource
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.RedeemableOfferEntity
import app.eluvio.wallet.data.entities.v2.SearchFiltersEntity
import app.eluvio.wallet.data.entities.v2.permissions.PermissionContext
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
        val sectionId: String

        // TODO: maybe combine Title and Description into a single "Text" Row type,
        //  but then we'd have to start passing around predefined styles or something
        @Immutable
        data class Title(override val sectionId: String, val text: AnnotatedString) : Section

        @Immutable
        data class Description(override val sectionId: String, val text: AnnotatedString) : Section

        @Immutable
        data class Banner(override val sectionId: String, val imagePath: String) : Section

        @Immutable
        data class Carousel(
            val permissionContext: PermissionContext,
            val title: String? = null,
            val subtitle: String? = null,
            val viewAllNavigationEvent: NavigationEvent? = null,
            val items: List<CarouselItem>,
            // Whether to show this as a row or a grid
            val showAsGrid: Boolean = false,
            val filterAttribute: SearchFiltersEntity.Attribute? = null,

            val backgroundColor: Color? = null,
            val backgroundImagePath: String? = null,
            val logoPath: String? = null,
            val logoText: String? = null,
        ) : Section {

            override val sectionId: String =
                requireNotNull(permissionContext.sectionId) { "PermissionContext.sectionId is null" }
        }
    }

    sealed interface CarouselItem {
        val permissionContext: PermissionContext

        @Immutable
        data class Media(
            override val permissionContext: PermissionContext,
            val entity: MediaEntity,
        ) : CarouselItem {
            // TODO: remove once MediaGrid is converted to use PermissionContext
            val propertyId: String = permissionContext.propertyId
        }

        @Immutable
        data class SubpropertyLink(
            override val permissionContext: PermissionContext,
            val subpropertyId: String,
            val imageUrl: String?,
            val imageAspectRatio: Float?,
            val title: String?,
            val subtitle: String?,
            val headers: List<String>
        ) : CarouselItem

        @Immutable
        data class RedeemableOffer(
            override val permissionContext: PermissionContext,
            val offerId: String,
            val name: String,
            val fulfillmentState: RedeemableOfferEntity.FulfillmentState,
            val contractAddress: String,
            val tokenId: String,
            val imageUrl: String?,
            val animation: MediaSource?,
        ) : CarouselItem

        @Immutable
        data class CustomCard(
            override val permissionContext: PermissionContext,
            val imageUrl: String?,
            val title: String,
            val aspectRatio: Float = 1f,
            val onClick: (() -> Unit)
        ) : CarouselItem

        @Immutable
        data class ItemPurchase(
            override val permissionContext: PermissionContext,
            val imageUrl: String?,
            val imageAspectRatio: Float?,
            val title: String?,
        ) : CarouselItem
    }
}
