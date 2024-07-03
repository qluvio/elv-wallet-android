package app.eluvio.wallet.screens.property.mediagrid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvGridItemSpan
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.RedeemableOfferEntity
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.screens.common.Overscan
import app.eluvio.wallet.screens.property.CarouselItemCard
import app.eluvio.wallet.screens.property.DynamicPageLayoutState
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.header_53
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@MainGraph
@Destination(navArgsDelegate = MediaGridNavArgs::class)
@Composable
fun MediaGrid() {
    hiltViewModel<MediaGridViewModel>().subscribeToState { vm, state ->
        MediaGrid(state)
    }
}

@Composable
private fun MediaGrid(state: MediaGridViewModel.State) {
    val cardSpacing = 20.dp
    val cardHeight = 200.dp
    TvLazyVerticalGrid(
        columns = TvGridCells.Adaptive(minSize = cardHeight),
        horizontalArrangement = Arrangement.spacedBy(cardSpacing),
        verticalArrangement = Arrangement.spacedBy(cardSpacing),
        contentPadding = Overscan.defaultPadding(),
        pivotOffsets = PivotOffsets(0.2f),
    ) {
        state.title?.let {
            item(span = { TvGridItemSpan(maxLineSpan) }) {
                Text(text = it, style = MaterialTheme.typography.header_53)
            }
        }
        items(state.items) { item ->
            CarouselItemCard(carouselItem = item, cardHeight = cardHeight)
        }
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun MediaGridPreview() = EluvioThemePreview {
    MediaGrid(MediaGridViewModel.State(
        title = "Grid Title!",
        items = listOf(
            DynamicPageLayoutState.CarouselItem.Media(
                entity = MediaEntity().apply {
                    id = "1"
                    name = "Media 1"
                    mediaType = "image"
                }
            ),
            DynamicPageLayoutState.CarouselItem.RedeemableOffer(
                offerId = "1",
                name = "Offer 1",
                fulfillmentState = RedeemableOfferEntity.FulfillmentState.AVAILABLE,
                contractAddress = "0x123",
                tokenId = "1",
                imageUrl = "https://via.placeholder.com/150",
                animation = null
            )
        )))
}
