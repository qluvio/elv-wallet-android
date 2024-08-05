package app.eluvio.wallet.screens.property

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
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
import app.eluvio.wallet.util.compose.fromHex

/**
 * The maximum number of items to display in a carousel before showing a "View All" button.
 */
private const val VIEW_ALL_THRESHOLD = 5

fun MediaPageSectionEntity.toCarousel(
    propertyId: String,
    filters: SearchFiltersEntity? = null,
    viewAllThreshold: Int = VIEW_ALL_THRESHOLD,
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
        showAsGrid = displayFormat == MediaPageSectionEntity.DisplayFormat.GRID,
        filterAttribute = filterAttribute,
        viewAllNavigationEvent = MediaGridDestination(
            propertyId = propertyId,
            sectionId = id
        )
            .takeIf { showViewAll }
            ?.asPush()
    )
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
        is CarouselItem.Media -> MediaItemCard(
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
