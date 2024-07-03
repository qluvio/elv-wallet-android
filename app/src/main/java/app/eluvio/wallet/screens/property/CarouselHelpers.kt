package app.eluvio.wallet.screens.property

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity.SectionItemEntity
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity.SectionItemEntity.Companion.MEDIA_CONTAINERS
import app.eluvio.wallet.screens.common.MediaItemCard
import app.eluvio.wallet.screens.property.DynamicPageLayoutState.CarouselItem
import app.eluvio.wallet.screens.property.items.OfferCard
import app.eluvio.wallet.screens.property.items.SubpropertyCard

fun List<SectionItemEntity>.toCarouselItems(): List<CarouselItem> {
    return flatMap { item ->
        when {
            item.subpropertyId != null -> {
                listOf(
                    CarouselItem.SubpropertyLink(
                        subpropertyId = item.subpropertyId!!,
                        imageUrl = item.subpropertyImage
                    )
                )
            }

            item.expand && item.mediaType in MEDIA_CONTAINERS -> {
                // TODO: Also expand media collections
                item.media?.mediaListItems.orEmpty()
                    .map { CarouselItem.Media(it) }
            }

            else -> {
                listOfNotNull(item.media?.let { CarouselItem.Media(it) })
            }
        }
    }
}

@Composable
fun CarouselItemCard(carouselItem: CarouselItem, cardHeight: Dp) =
    when (carouselItem) {
        is CarouselItem.Media -> MediaItemCard(
            carouselItem.entity,
            cardHeight = cardHeight
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
