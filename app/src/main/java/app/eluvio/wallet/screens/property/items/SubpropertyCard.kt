package app.eluvio.wallet.screens.property.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
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
import app.eluvio.wallet.screens.destinations.PropertyDetailDestination
import app.eluvio.wallet.screens.property.DynamicPageLayoutState
import app.eluvio.wallet.theme.label_24

@Composable
fun SubpropertyCard(
    item: DynamicPageLayoutState.CarouselItem.SubpropertyLink,
    cardHeight: Dp,
    modifier: Modifier = Modifier
) {
    Column {
        val navigator = LocalNavigator.current
        ImageCard(
            imageUrl = item.imageUrl,
            contentDescription = item.title,
            focusedOverlay = {
                MetadataTexts(headers = item.headers, title = item.title, subtitle = item.subtitle)
            },
            onClick = { navigator(PropertyDetailDestination(item.subpropertyId).asPush()) },
            modifier = modifier
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
