package app.eluvio.wallet.screens.property.items

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import app.eluvio.wallet.screens.common.ImageCard
import app.eluvio.wallet.screens.property.DynamicPageLayoutState

@Composable
fun BannerItem(
    item: DynamicPageLayoutState.CarouselItem.BannerWrapper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: ImageCard might be overkill here, we might just want an AsyncImage with a border
    ImageCard(
        item.bannerImageUrl,
        contentDescription = "Banner",
        onClick = onClick,
        shape = RectangleShape,
        modifier = modifier.fillMaxWidth()
    )
    // TODO: add title/subtitle
}
