package app.eluvio.wallet.screens.dashboard.myitems

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.Text
import app.eluvio.wallet.app.Events
import app.eluvio.wallet.navigation.DashboardTabsGraph
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.common.EluvioLoadingSpinner
import app.eluvio.wallet.screens.destinations.NftDetailDestination
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination
import java.util.UUID
import kotlin.math.roundToInt

@DashboardTabsGraph(start = true)
@Destination
@Composable
fun MyItems() {
    val context = LocalContext.current
    hiltViewModel<MyItemsViewModel>().subscribeToState(
        onEvent = {
            when (it) {
                Events.NetworkError -> Toast.makeText(
                    context,
                    "Network error. Please try again later.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        onState = { _, state ->
            MyItems(state)
        }
    )
}

@Composable
private fun MyItems(state: AllMediaProvider.State) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.loading) {
            EluvioLoadingSpinner()
        } else if (state.media.isEmpty()) {
            Text("No items to display")
        } else {
            val navigator = LocalNavigator.current
            val context = LocalContext.current
            MyItemsGrid(state.media, onItemClick = {
                if (it.tokenId == null) {
                    Toast.makeText(context, "NFT Packs not supported yet", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    navigator(NftDetailDestination(it.contractAddress, it.tokenId).asPush())
                }
            })
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.MyItemsGrid(
    media: List<AllMediaProvider.State.Media>,
    onItemClick: (AllMediaProvider.State.Media) -> Unit
) {
    val width by rememberUpdatedState(maxWidth)
    val horizontalPadding = 100.dp
    val cardSpacing = 20.dp
    val desiredCardWidth = 240.dp
    val columnCount by remember {
        derivedStateOf {
            val availableWidth = width - horizontalPadding
            val cardWidth = desiredCardWidth + cardSpacing
            (availableWidth / cardWidth).roundToInt()
        }
    }
    TvLazyVerticalGrid(
        columns = TvGridCells.Fixed(columnCount),
        horizontalArrangement = Arrangement.spacedBy(cardSpacing),
        verticalArrangement = Arrangement.spacedBy(cardSpacing),
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 20.dp),
        pivotOffsets = PivotOffsets(0.1f),
        modifier = Modifier.fillMaxSize()
    ) {
        items(media, key = { it.key }) { mediaItem ->
            MediaCard(
                mediaItem,
                onClick = { onItemClick(mediaItem) },
            )
        }
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun MyItemsPreview() = EluvioThemePreview {
    val items = listOf(
        AllMediaProvider.State.Media(
            "key",
            "contract_address",
            "https://x",
            "Single Token",
            "Special Edition",
            "1",
            1
        ),
        AllMediaProvider.State.Media(
            "key",
            "contract_address",
            "https://x",
            "Token Pack",
            "Pleab Edition",
            null,
            53
        )
    )
    MyItems(AllMediaProvider.State(
        loading = false,
        // create 10 copies of the original list
        (1..10).flatMap { items }.map { it.copy(key = UUID.randomUUID().toString()) }
    ))
}

@Composable
@Preview(device = Devices.TV_720p)
private fun MyItemsPreviewLoading() = EluvioThemePreview {
    MyItems(AllMediaProvider.State(loading = true))
}
