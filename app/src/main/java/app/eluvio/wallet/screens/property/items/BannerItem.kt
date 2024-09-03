package app.eluvio.wallet.screens.property.items

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import app.eluvio.wallet.screens.property.DynamicPageLayoutState
import app.eluvio.wallet.theme.LocalSurfaceScale
import app.eluvio.wallet.theme.borders
import app.eluvio.wallet.theme.focusedBorder
import coil.compose.AsyncImage

@Composable
fun BannerItem(
    item: DynamicPageLayoutState.CarouselItem.BannerWrapper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        border = MaterialTheme.borders.focusedBorder,
        scale = LocalSurfaceScale.current,
        shape = ClickableSurfaceDefaults.shape(RectangleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent
        ),
        modifier = modifier
    ) {
        AsyncImage(
            model = item.bannerImageUrl,
            contentScale = ContentScale.Crop,
            contentDescription = "Banner",
            modifier = modifier.fillMaxWidth()
        )
    }
    // TODO: add title/subtitle
}
