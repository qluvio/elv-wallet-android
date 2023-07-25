package app.eluvio.wallet.screens.dashboard.mymedia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.navigation.DashboardTabsGraph
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.common.MediaItemCard
import app.eluvio.wallet.screens.dashboard.myitems.AllMediaProvider
import app.eluvio.wallet.screens.dashboard.myitems.MediaCard
import app.eluvio.wallet.screens.destinations.NftDetailDestination
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.carousel_36
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@DashboardTabsGraph
@Destination
@Composable
fun MyMedia() {
    hiltViewModel<MyMediaViewModel>().subscribeToState { _, state ->
        MyMedia(state)
    }
}

@Composable
private fun MyMedia(state: MyMediaViewModel.State) {
    TvLazyColumn(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(10.dp),
        pivotOffsets = PivotOffsets(0.1f),
        modifier = Modifier.fillMaxSize()
    ) {
        // needs to be portrait aspect ratio
        item {
            FeaturedMediaRow(state.featuredMedia, state.baseUrl)
        }
        // TODO add section media
        items(state.nftMedia.entries.toList()) { (displayName, mediaItems) ->
            NftMediaRow(displayName, mediaItems)
        }
        if (state.myItems.isNotEmpty()) {
            item {
                MyItemsRow(state.myItems)
            }
        }
    }
}

@Composable
fun FeaturedMediaRow(featuredMedia: List<MediaEntity>, baseUrl: String?) {
    TvLazyRow(
        contentPadding = PaddingValues(10.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(featuredMedia) { media ->
            val url = if (media.posterImagePath != null && baseUrl != null) {
                "$baseUrl${media.posterImagePath}"
            } else {
                media.image
            }
            MediaItemCard(
                media = media,
                imageUrl = url,
                shape = RoundedCornerShape(0.dp),
                cardHeight = 300.dp,
                aspectRatio = MediaEntity.ASPECT_RATIO_POSTER,
            )
        }
    }
}

@Composable
fun NftMediaRow(displayName: String, mediaItems: List<MediaEntity>) {
    RowHeader(displayName)
    Spacer(Modifier.height(10.dp))
    TvLazyRow(
        contentPadding = PaddingValues(10.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(mediaItems) { media ->
            MediaItemCard(media)
        }
    }
}

@Composable
private fun MyItemsRow(myItems: List<AllMediaProvider.State.Media>) {
    RowHeader(text = "Items")
    TvLazyRow(
        contentPadding = PaddingValues(10.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(myItems) { item ->
            val navigator = LocalNavigator.current
            MediaCard(
                media = item,
                onClick = {
                    if (item.tokenId != null) {
                        navigator(NftDetailDestination(item.contractAddress, item.tokenId).asPush())
                    }
                },
                modifier = Modifier.width(200.dp)
            )
        }
    }
}

@Composable
private fun RowHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.carousel_36,
        modifier = Modifier.padding(start = 10.dp)
    )
}

@Composable
@Preview(device = Devices.TV_720p)
private fun MyMediaPreview() = EluvioThemePreview {
    MyMedia(MyMediaViewModel.State())
}
