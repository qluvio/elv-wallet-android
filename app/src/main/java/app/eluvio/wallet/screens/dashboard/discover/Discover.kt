package app.eluvio.wallet.screens.dashboard.discover

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvGridItemSpan
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.itemsIndexed
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.data.entities.v2.MediaPropertyEntity
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.common.EluvioLoadingSpinner
import app.eluvio.wallet.screens.common.ShimmerImage
import app.eluvio.wallet.screens.common.requestInitialFocus
import app.eluvio.wallet.screens.destinations.PropertyDetailDestination
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.util.subscribeToState
import kotlin.math.roundToInt

@Composable
fun Discover(onBackgroundImageSet: (String?) -> Unit) {
    hiltViewModel<DiscoverViewModel>().subscribeToState { _, state ->
        Discover(state, onBackgroundImageSet)
    }
}

@Composable
private fun Discover(
    state: DiscoverViewModel.State,
    onBackgroundImageSet: (String?) -> Unit,
) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.loading) {
            EluvioLoadingSpinner()
        } else if (state.properties.isEmpty()) {
            Text(stringResource(R.string.no_content_warning))
        } else {
            val navigator = LocalNavigator.current
            DiscoverGrid(
                state,
                onPropertyFocused = { property ->
                    val bgImage = property.mainPage?.backgroundImagePath?.let {
                        "${state.baseUrl}/${it}"
                    }
                    onBackgroundImageSet(bgImage)
                },
                onPropertyClicked = {
                    navigator(PropertyDetailDestination(it.id).asPush())
                }
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.DiscoverGrid(
    state: DiscoverViewModel.State,
    onPropertyFocused: (MediaPropertyEntity) -> Unit,
    onPropertyClicked: (MediaPropertyEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val width by rememberUpdatedState(maxWidth)
    val horizontalPadding = 50.dp
    val cardSpacing = 15.dp
    val desiredCardWidth = 170.dp
    val columnCount = remember(width, horizontalPadding, desiredCardWidth, cardSpacing) {
        val availableWidth = width - horizontalPadding
        val cardWidth = desiredCardWidth + cardSpacing
        (availableWidth / cardWidth).roundToInt()
    }
    TvLazyVerticalGrid(
        columns = TvGridCells.Fixed(columnCount),
        horizontalArrangement = Arrangement.spacedBy(cardSpacing),
        verticalArrangement = Arrangement.spacedBy(cardSpacing),
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        pivotOffsets = PivotOffsets(0.6f),
    ) {
        item(span = { TvGridItemSpan(maxLineSpan) }) {
            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.height(180.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.discover_logo),
                    contentDescription = "Eluvio Logo",
                    modifier = Modifier.fillMaxWidth(0.4f)
                )
            }
        }
        itemsIndexed(state.properties) { index, property ->
            Surface(
                onClick = { onPropertyClicked(property) },
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(2.dp)),
                modifier = Modifier
                    .onFocusChanged {
                        if (it.hasFocus) {
                            onPropertyFocused(property)
                        }
                    }
                    .then(
                        if (index == 0)
                            Modifier.requestInitialFocus()
                        else
                            Modifier
                    )
            ) {
                ShimmerImage(
                    model = "${state.baseUrl}/${property.image}",
                    contentDescription = property.name
                )
            }
        }
        item(span = { TvGridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun DiscoverPreview() = EluvioThemePreview {
    Discover(DiscoverViewModel.State(), onBackgroundImageSet = {})
}
