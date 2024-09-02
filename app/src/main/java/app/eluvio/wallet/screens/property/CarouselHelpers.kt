package app.eluvio.wallet.screens.property

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import app.eluvio.wallet.data.entities.v2.DisplayFormat
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
import app.eluvio.wallet.data.entities.v2.SearchFiltersEntity
import app.eluvio.wallet.data.entities.v2.SectionItemEntity
import app.eluvio.wallet.data.entities.v2.permissions.PermissionContext
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.destinations.MediaGridDestination
import app.eluvio.wallet.screens.property.DynamicPageLayoutState.CarouselItem
import app.eluvio.wallet.util.compose.fromHex
import app.eluvio.wallet.util.logging.Log

/**
 * The maximum number of items to display in a carousel before showing a "View All" button.
 */
private const val VIEW_ALL_THRESHOLD = 5

/**
 * Converts a [MediaPageSectionEntity] to a list of [DynamicPageLayoutState.Section]s.
 * Usually this will be a single section, but in the case of a hero section, it may be multiple.
 */
fun MediaPageSectionEntity.toDynamicSections(
    parentPermissionContext: PermissionContext,
    filters: SearchFiltersEntity? = null,
    viewAllThreshold: Int = VIEW_ALL_THRESHOLD,
): List<DynamicPageLayoutState.Section> {
    return when (type) {
        MediaPageSectionEntity.TYPE_AUTOMATIC,
        MediaPageSectionEntity.TYPE_MANUAL,
        MediaPageSectionEntity.TYPE_SEARCH -> listOf(
            this.toCarouselSection(
                parentPermissionContext,
                filters,
                viewAllThreshold,
            )
        )

        MediaPageSectionEntity.TYPE_HERO -> this.toHeroSections()
        else -> emptyList()
    }
}

private fun MediaPageSectionEntity.toCarouselSection(
    parentPermissionContext: PermissionContext,
    filters: SearchFiltersEntity? = null,
    viewAllThreshold: Int = VIEW_ALL_THRESHOLD,
): DynamicPageLayoutState.Section.Carousel {
    val permissionContext = parentPermissionContext.copy(sectionId = id)
    val items = items.toCarouselItems(permissionContext, displayFormat)
    val displayLimit = displayLimit?.takeIf { it > 0 } ?: items.size
    val showViewAll = items.size > displayLimit || items.size > viewAllThreshold
    val filterAttribute = primaryFilter?.let { primaryFilter ->
        filters?.attributes?.firstOrNull { it.id == primaryFilter }
    }
    return DynamicPageLayoutState.Section.Carousel(
        permissionContext,
        title = title,
        subtitle = subtitle,
        items = items.take(displayLimit),
        backgroundColor = backgroundColor?.let { Color.fromHex(it) },
        backgroundImagePath = backgroundImagePath,
        logoPath = logoPath,
        logoText = logoText,
        displayFormat = displayFormat,
        filterAttribute = filterAttribute,
        viewAllNavigationEvent = MediaGridDestination(
            propertyId = parentPermissionContext.propertyId,
            sectionId = id
        )
            .takeIf { showViewAll }
            ?.asPush()
    )
}

private fun MediaPageSectionEntity.toHeroSections(): List<DynamicPageLayoutState.Section> {
    return items.flatMap { item ->
        val sectionIdPrefix = "${this.id}-${item.id}"
        listOfNotNull(
            item.logoPath?.let {
                DynamicPageLayoutState.Section.Banner("${sectionIdPrefix}-banner", it)
            },
            item.title?.let {
                DynamicPageLayoutState.Section.Title(
                    sectionId = "$sectionIdPrefix-title",
                    text = AnnotatedString(it)
                )
            },
            item.description?.let {
                DynamicPageLayoutState.Section.Description(
                    sectionId = "$sectionIdPrefix-description",
                    text = AnnotatedString(it)
                )
            }
        )
    }
}

fun List<SectionItemEntity>.toCarouselItems(
    parentPermissionContext: PermissionContext,
    sectionDisplayFormat: DisplayFormat
): List<CarouselItem> {
    return mapNotNull { item ->
        val bannerImage = item.bannerImageUrl
        val isBannerSection = sectionDisplayFormat == DisplayFormat.BANNER
        if (isBannerSection && bannerImage == null) {
            Log.w("Section item inside a Banner section, doesn't have a banner image configured")
            return@mapNotNull null
        }
        val permissionContext = parentPermissionContext.copy(sectionItemId = item.id)
        val result = when {
            // Filter out hidden items
            item.isHidden -> null

            item.linkData != null -> {
                CarouselItem.PageLink(
                    permissionContext = permissionContext,
                    // If linkData doesn't have a propertyId,
                    // assume this is a link to page within the current property.
                    propertyId = item.linkData?.linkPropertyId ?: permissionContext.propertyId,
                    pageId = item.linkData?.linkPageId,
                    imageUrl = item.thumbnailUrl,
                    imageAspectRatio = item.thumbnailAspectRatio,
                    title = item.title,
                    subtitle = item.subtitle,
                    headers = item.headers
                )
            }

            item.media != null -> CarouselItem.Media(
                permissionContext = permissionContext.copy(mediaItemId = item.media!!.id),
                entity = item.media!!,
            )

            item.isPurchaseItem -> {
                CarouselItem.ItemPurchase(
                    permissionContext = permissionContext,
                    title = item.title,
                    imageUrl = item.thumbnailUrl,
                    imageAspectRatio = item.thumbnailAspectRatio,
                )
            }

            else -> null
        }

        // Wrap in a banner if necessary, otherwise return as-is
        if (result != null && isBannerSection && bannerImage != null) {
            result.asBanner(bannerImage)
        } else {
            result
        }
    }
}
