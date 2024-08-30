package app.eluvio.wallet.screens.property.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.v2.permissions.PermissionContext
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.common.MediaItemCard
import app.eluvio.wallet.screens.common.defaultMediaItemClickHandler
import app.eluvio.wallet.screens.destinations.MediaGridDestination
import app.eluvio.wallet.screens.destinations.PropertyDetailDestination
import app.eluvio.wallet.screens.destinations.PurchasePromptDestination
import app.eluvio.wallet.screens.destinations.UpcomingVideoDestination
import app.eluvio.wallet.screens.property.DynamicPageLayoutState.CarouselItem
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.disabledItemAlpha
import app.eluvio.wallet.theme.label_24
import app.eluvio.wallet.util.compose.thenIf

@Composable
fun CarouselItemCard(carouselItem: CarouselItem, cardHeight: Dp, modifier: Modifier = Modifier) {
    val navigator = LocalNavigator.current
    when (carouselItem) {
        is CarouselItem.Media -> Column(modifier = modifier.width(IntrinsicSize.Min)) {
            val entity = carouselItem.entity
            MediaItemCard(
                entity,
                cardHeight = cardHeight,
                onMediaItemClick = { media ->
                    when {
                        media.showAlternatePage -> {
                            navigator(
                                PropertyDetailDestination(
                                    propertyId = carouselItem.permissionContext.propertyId,
                                    pageId = media.resolvedPermissions?.alternatePageId!!
                                ).asPush()
                            )
                        }

                        media.showPurchaseOptions -> {
                            navigator(
                                PurchasePromptDestination(carouselItem.permissionContext)
                                    .asPush()
                            )
                        }

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
                entity.name,
                style = MaterialTheme.typography.label_24.copy(fontSize = 10.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.thenIf(entity.isDisabled) {
                    alpha(MaterialTheme.colorScheme.disabledItemAlpha)
                }
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

        is CarouselItem.ItemPurchase -> ItemPurchaseCard(
            item = carouselItem,
            cardHeight = cardHeight
        )
    }
}

@Preview(widthDp = 250, heightDp = 250)
@Composable
private fun CarouselItemCardPreview() = EluvioThemePreview {
    CarouselItemCard(
        carouselItem = CarouselItem.Media(
            permissionContext = PermissionContext(propertyId = "property"),
            entity = MediaEntity().apply {
                name = "this is a very very very very long title"
            },
        ), 120.dp
    )
}
