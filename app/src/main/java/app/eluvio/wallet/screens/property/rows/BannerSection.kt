package app.eluvio.wallet.screens.property.rows

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.eluvio.wallet.screens.common.Overscan
import app.eluvio.wallet.screens.property.DynamicPageLayoutState
import coil.compose.AsyncImage

@Composable
fun BannerSection(
    item: DynamicPageLayoutState.Section.Banner,
    baseUrl: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = baseUrl?.let { "$it${item.imagePath}" },
        contentDescription = "Logo",
        modifier
            .heightIn(max = 90.dp)
            .padding(
                start = Overscan.horizontalPadding,
                end = 260.dp
            )
    )
}
