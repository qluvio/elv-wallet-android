package app.eluvio.wallet.screens.property

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity.SectionItemEntity
import app.eluvio.wallet.data.entities.v2.SearchFiltersEntity
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.destinations.MediaGridDestination
import app.eluvio.wallet.screens.property.DynamicPageLayoutState.CarouselItem
import app.eluvio.wallet.util.compose.fromHex

/**
 * The maximum number of items to display in a carousel before showing a "View All" button.
 */
private const val VIEW_ALL_THRESHOLD = 5

/**
 * Converts a [MediaPageSectionEntity] to a list of [DynamicPageLayoutState.Section]s.
 * Usually this will be a single section, but in the case of a hero section, it may be multiple.
 */
fun MediaPageSectionEntity.toDynamicSections(
    propertyId: String,
    filters: SearchFiltersEntity? = null,
    viewAllThreshold: Int = VIEW_ALL_THRESHOLD,
    forceGridView: Boolean = false,
): List<DynamicPageLayoutState.Section> {
    return when (type) {
        MediaPageSectionEntity.TYPE_AUTOMATIC,
        MediaPageSectionEntity.TYPE_MANUAL,
        MediaPageSectionEntity.TYPE_SEARCH -> listOf(
            this.toCarouselSection(
                propertyId,
                filters,
                viewAllThreshold,
                forceGridView
            )
        )

        MediaPageSectionEntity.TYPE_HERO -> this.toHeroSections()
        else -> emptyList()
    }
}

private fun MediaPageSectionEntity.toCarouselSection(
    propertyId: String,
    filters: SearchFiltersEntity? = null,
    viewAllThreshold: Int = VIEW_ALL_THRESHOLD,
    forceGridView: Boolean,
): DynamicPageLayoutState.Section.Carousel {
    val items = items.toCarouselItems(propertyId)
    val displayLimit = displayLimit?.takeIf { it > 0 } ?: items.size
    val showViewAll = items.size > displayLimit || items.size > viewAllThreshold
    val filterAttribute = primaryFilter?.let { primaryFilter ->
        filters?.attributes?.firstOrNull { it.id == primaryFilter }
    }
    return DynamicPageLayoutState.Section.Carousel(
        sectionId = id,
        title = title,
        subtitle = subtitle,
        items = items.take(displayLimit),
        backgroundColor = backgroundColor?.let { Color.fromHex(it) },
        backgroundImagePath = backgroundImagePath,
        logoPath = logoPath,
        logoText = logoText,
        showAsGrid = forceGridView || displayFormat == MediaPageSectionEntity.DisplayFormat.GRID,
        filterAttribute = filterAttribute,
        viewAllNavigationEvent = MediaGridDestination(
            propertyId = propertyId,
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

fun List<SectionItemEntity>.toCarouselItems(propertyId: String): List<CarouselItem> {
    return mapNotNull { item ->
        when {
            item.subpropertyId != null -> {
                CarouselItem.SubpropertyLink(
                    subpropertyId = item.subpropertyId!!,
                    imageUrl = item.subpropertyImage
                )
            }

            item.media != null -> CarouselItem.Media(item.media!!, propertyId)

            else -> null
        }
    }
}
