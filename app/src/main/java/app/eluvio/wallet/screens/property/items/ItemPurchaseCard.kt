package app.eluvio.wallet.screens.property.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.data.AspectRatio
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.common.ImageCard
import app.eluvio.wallet.screens.common.MetadataTexts
import app.eluvio.wallet.screens.destinations.PurchasePromptDestination
import app.eluvio.wallet.screens.property.DynamicPageLayoutState
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.label_24

@Composable
fun ItemPurchaseCard(
    item: DynamicPageLayoutState.CarouselItem.ItemPurchase,
    cardHeight: Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.width(IntrinsicSize.Min)) {
        val navigator = LocalNavigator.current
        ImageCard(
            imageUrl = item.imageUrl,
            contentDescription = item.title,
            focusedOverlay = {
                MetadataTexts(title = item.title, subtitle = null, headers = emptyList())
            },
            onClick = {
                navigator(
                    PurchasePromptDestination(
                        sectionItemId = item.sectionItemId,
                        propertyId = item.propertyId
                    ).asPush()
                )
            },
            modifier = Modifier
                .height(cardHeight)
                .aspectRatio(
                    item.imageAspectRatio ?: AspectRatio.WIDE,
                    matchHeightConstraintsFirst = true
                )
        )
        item.title?.let { title ->
            Spacer(Modifier.height(10.dp))
            Text(
                title,
                style = MaterialTheme.typography.label_24.copy(fontSize = 10.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(widthDp = 200, heightDp = 200)
@Composable
private fun ItemPurchaseCardPreview() = EluvioThemePreview {
    ItemPurchaseCard(
        item = DynamicPageLayoutState.CarouselItem.ItemPurchase(
            propertyId = "property_id",
            sectionItemId = "section_id",
            purchaseUrl = "https://www.google.com",
            title = "Title that is really really really really really really really really long",
            imageUrl = "https://www.google.com",
            imageAspectRatio = AspectRatio.SQUARE
        ), cardHeight = 150.dp
    )
}
