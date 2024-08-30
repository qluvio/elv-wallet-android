package app.eluvio.wallet.screens.property.items

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.data.AspectRatio
import app.eluvio.wallet.data.entities.v2.permissions.PermissionContext
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.common.ImageCard
import app.eluvio.wallet.screens.common.MetadataTexts
import app.eluvio.wallet.screens.destinations.PropertyDetailDestination
import app.eluvio.wallet.screens.property.DynamicPageLayoutState
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.label_24

@Composable
fun SubpropertyCard(
    item: DynamicPageLayoutState.CarouselItem.SubpropertyLink,
    cardHeight: Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier.width(IntrinsicSize.Min)) {
        val navigator = LocalNavigator.current
        ImageCard(
            imageUrl = item.imageUrl,
            contentDescription = item.title,
            focusedOverlay = {
                MetadataTexts(headers = item.headers, title = item.title, subtitle = item.subtitle)
            },
            onClick = { navigator(PropertyDetailDestination(propertyId = item.subpropertyId).asPush()) },
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
private fun SubpropertyCardPreview(modifier: Modifier = Modifier) = EluvioThemePreview {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        SubpropertyCard(
            item = DynamicPageLayoutState.CarouselItem.SubpropertyLink(
                permissionContext = PermissionContext(propertyId = "property1"),
                subpropertyId = "subId",
                imageUrl = "img",
                imageAspectRatio = 1f,
                subtitle = "subtitle",
                title = "title",
                headers = listOf("header1", "header2")
            ),
            cardHeight = 100.dp,
        )
    }
}
