package app.eluvio.wallet.screens.property

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity.SectionItemEntity
import app.eluvio.wallet.data.entities.v2.SearchFiltersEntity
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.common.MediaItemCard
import app.eluvio.wallet.screens.common.defaultMediaItemClickHandler
import app.eluvio.wallet.screens.destinations.MediaGridDestination
import app.eluvio.wallet.screens.destinations.UpcomingVideoDestination
import app.eluvio.wallet.screens.property.DynamicPageLayoutState.CarouselItem
import app.eluvio.wallet.screens.property.items.CustomCard
import app.eluvio.wallet.screens.property.items.OfferCard
import app.eluvio.wallet.screens.property.items.SubpropertyCard
import app.eluvio.wallet.theme.label_24
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

@Composable
fun CarouselItemCard(carouselItem: CarouselItem, cardHeight: Dp, modifier: Modifier = Modifier) {
    val navigator = LocalNavigator.current
    when (carouselItem) {
        is CarouselItem.Media -> Column {
            MediaItemCard(
                carouselItem.entity,
                modifier = modifier,
                cardHeight = cardHeight,
                onMediaItemClick = { media ->
                    when {
                        media.mediaItemsIds.isNotEmpty() -> {
                            // This media item is a container for other media (e.g. a media list/collection)
                            navigator(
                                MediaGridDestination(
                                    propertyId = carouselItem.propertyId,
                                    mediaContainerId = media.id
                                ).asPush()
                            )
                        }

                        media.liveVideoInfo?.started == false -> {
                            // this is a live video that hasn't started yet.
                            navigator(
                                UpcomingVideoDestination(
                                    propertyId = carouselItem.propertyId,
                                    mediaItemId = media.id,
                                ).asPush()
                            )
                        }

                        else -> {
                            defaultMediaItemClickHandler(navigator).invoke(media)
                        }
                    }
                }
            )
            Spacer(Modifier.height(10.dp))
            Text(
                carouselItem.entity.name,
                style = MaterialTheme.typography.label_24.copy(fontSize = 10.sp)
            )
        }

        is CarouselItem.RedeemableOffer -> OfferCard(
            carouselItem,
            cardHeight
        )

        is CarouselItem.SubpropertyLink -> SubpropertyCard(
            carouselItem,
            cardHeight
        )

        is CarouselItem.CustomCard -> {
            CustomCard(carouselItem, cardHeight, modifier)
        }
    }
}
