package app.eluvio.wallet.screens.property

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity.SectionItemEntity
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.common.MediaItemCard
import app.eluvio.wallet.screens.common.defaultMediaItemClickHandler
import app.eluvio.wallet.screens.destinations.MediaGridDestination
import app.eluvio.wallet.screens.destinations.UpcomingVideoDestination
import app.eluvio.wallet.screens.property.DynamicPageLayoutState.CarouselItem
import app.eluvio.wallet.screens.property.items.OfferCard
import app.eluvio.wallet.screens.property.items.SubpropertyCard

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
fun CarouselItemCard(carouselItem: CarouselItem, cardHeight: Dp) {
    val navigator = LocalNavigator.current
    when (carouselItem) {
        is CarouselItem.Media -> MediaItemCard(
            carouselItem.entity,
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
    }
}
