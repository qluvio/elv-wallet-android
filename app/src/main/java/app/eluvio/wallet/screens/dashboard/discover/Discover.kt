package app.eluvio.wallet.screens.dashboard.discover

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.onGloballyPositioned
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
import androidx.tv.foundation.lazy.grid.rememberTvLazyGridState
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.data.entities.v2.MediaPropertyEntity
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.common.EluvioLoadingSpinner
import app.eluvio.wallet.screens.common.ShimmerImage
import app.eluvio.wallet.screens.destinations.PropertyDetailDestination
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.util.compose.requestInitialFocus
import app.eluvio.wallet.util.compose.thenIf
import app.eluvio.wallet.util.isKeyUpOf
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.subscribeToState
import kotlinx.coroutines.launch
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
                        "${state.baseUrl}${it}"
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
    val scrollState = rememberTvLazyGridState()
    val scope = rememberCoroutineScope()

    // The focus problems here were hard to solve, I got some hints from:
    // https://stackoverflow.com/questions/76281554/android-jetpack-compose-tv-focus-restoring
    // But ultimately I had to make some adjustments because we also wanted to restore focus when
    // moving to the nav drawer and coming back, as well as focusing the first item on launch.

    /**
     * Always keep track of the last focused property, but there's more logic involved in actually
     * restoring focus to it.
     */
    var currentFocusedProperty by rememberSaveable { mutableStateOf<String?>(null) }

    /**
     * We save a clicked property, so we can restore focus to it when navigating back to this
     * screen. This is different than restoring focus when navigating between other elements on screen.
     */
    var lastClickedProperty by rememberSaveable { mutableStateOf<String?>(null) }

    /**
     * This is a trigger to let the corresponding Property item know that it should request focus
     * right now.
     */
    var onDemandFocusRestore by rememberSaveable { mutableStateOf<String?>(null) }

    val properties = state.properties
    TvLazyVerticalGrid(
        state = scrollState,
        columns = TvGridCells.Fixed(columnCount),
        horizontalArrangement = Arrangement.spacedBy(cardSpacing),
        verticalArrangement = Arrangement.spacedBy(cardSpacing),
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        pivotOffsets = PivotOffsets(0.3f),
        modifier = Modifier
            .onFocusChanged {
                if (it.hasFocus && lastClickedProperty == null) {
                    // We're gaining focus, but don't have a last clicked property: this means we
                    // are gaining focus back from an element on screen, rather than coming back
                    // from a different screen.
                    onDemandFocusRestore = currentFocusedProperty
                }
            }
            .onPreviewKeyEvent {
                val firstPropertyId = properties.first().id
                if (it.isKeyUpOf(Key.Back) && currentFocusedProperty != firstPropertyId) {
                    // User clicked back while not focused on first item. Scroll to top and
                    // trigger focus request.
                    scope.launch {
                        scrollState.animateScrollToItem(0)
                    }
                    onDemandFocusRestore = firstPropertyId
                    return@onPreviewKeyEvent true
                }
                false
            }
    ) {
        item(contentType = { "header" }, span = { TvGridItemSpan(maxLineSpan) }) {
            Column {
                Spacer(Modifier.height(60.dp))
                Image(
                    painter = painterResource(id = R.drawable.discover_logo),
                    contentDescription = "Eluvio Logo",
                    modifier = Modifier.height(70.dp)
                )
                Spacer(Modifier.height(30.dp))
            }
        }
        itemsIndexed(
            properties,
            contentType = { _, _ -> "property_card" },
            key = { _, property -> property.id }
        ) { index, property ->
            val focusRequester = remember { FocusRequester() }
            Surface(
                onClick = {
                    lastClickedProperty = property.id
                    onPropertyClicked(property)
                },
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(BorderStroke(2.dp, MaterialTheme.colorScheme.border))
                ),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(2.dp)),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .thenIf(property.id == lastClickedProperty) {
                        onGloballyPositioned {
                            Log.e("Restoring focus after navigation to ${property.id} ")
                            focusRequester.requestFocus()
                        }
                    }
                    .onFocusChanged {
                        if (it.hasFocus) {
                            currentFocusedProperty = property.id
                            // When any items gains focus, clear lastClickedProperty. It either
                            // doesn't need handling, or has already been handled.
                            lastClickedProperty = null
                            onPropertyFocused(property)
                        }
                    }
                    .thenIf(index == 0) {
                        // First property will ask for focus only for the very first composition.
                        requestInitialFocus()
                    }
            ) {
                ShimmerImage(
                    model = "${state.baseUrl}${property.image}",
                    contentDescription = property.name
                )
            }
            LaunchedEffect(property.id == onDemandFocusRestore) {
                if (property.id == onDemandFocusRestore) {
                    Log.e("On-demand focus restore for: ${property.id}")
                    onDemandFocusRestore = null
                    focusRequester.requestFocus()
                    // +1 because header is at index 0
                    scrollState.animateScrollToItem(index + 1)
                }
            }
        }
        item(contentType = { "footer" }, span = { TvGridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun DiscoverPreview() = EluvioThemePreview {
    Discover(DiscoverViewModel.State(), onBackgroundImageSet = {})
}
