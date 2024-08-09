package app.eluvio.wallet.screens.property.mediagrid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.RedeemableOfferEntity
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.screens.common.DelayedFullscreenLoader
import app.eluvio.wallet.screens.common.Overscan
import app.eluvio.wallet.screens.property.DynamicPageLayoutState.CarouselItem
import app.eluvio.wallet.screens.property.items.CarouselItemCard
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.body_32
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@MainGraph
@Destination(navArgsDelegate = MediaGridNavArgs::class)
@Composable
fun MediaGrid() {
    hiltViewModel<MediaGridViewModel>().subscribeToState { vm, state ->
        if (state.loading) {
            DelayedFullscreenLoader()
        } else {
            MediaGrid(state)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MediaGrid(state: MediaGridViewModel.State) {
    val cardHeight = 120.dp
    FlowRow(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalArrangement = Arrangement.spacedBy(22.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xFF16151F), Color(0xFF0C0C10))))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 27.dp, vertical = Overscan.verticalPadding)
    ) {
        state.title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.body_32,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start=7.dp)
            )
        }
        state.items.forEach { item ->
            CarouselItemCard(
                carouselItem = item,
                cardHeight = cardHeight,
                modifier = Modifier.padding(horizontal = 7.dp)
            )
        }
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun MediaGridPreview() = EluvioThemePreview {
    MediaGrid(
        MediaGridViewModel.State(
            title = "Grid Title!",
            items = listOf(
                CarouselItem.Media(
                    entity = MediaEntity().apply {
                        id = "1"
                        name = "Media 1"
                        mediaType = "image"
                    },
                    propertyId = "property1"
                ),
                CarouselItem.RedeemableOffer(
                    offerId = "1",
                    name = "Offer 1",
                    fulfillmentState = RedeemableOfferEntity.FulfillmentState.AVAILABLE,
                    contractAddress = "0x123",
                    tokenId = "1",
                    imageUrl = "https://via.placeholder.com/150",
                    animation = null
                )
            )
        )
    )
}
