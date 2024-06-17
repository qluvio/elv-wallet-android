package app.eluvio.wallet.screens.property.rows

import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.eluvio.wallet.screens.property.DynamicPageLayoutState
import coil.compose.AsyncImage

@Composable
 fun BannerRow(
    item: DynamicPageLayoutState.Row.Banner,
    state: DynamicPageLayoutState,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = state.urlForPath(item.imagePath),
        contentDescription = "Logo",
        modifier.offset(x = 28.dp)
    )
}
