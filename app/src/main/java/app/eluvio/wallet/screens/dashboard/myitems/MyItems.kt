package app.eluvio.wallet.screens.dashboard.myitems

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.app.Events
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.common.EluvioLoadingSpinner
import app.eluvio.wallet.screens.common.Overscan
import app.eluvio.wallet.screens.common.SearchBox
import app.eluvio.wallet.screens.common.SearchFilterChip
import app.eluvio.wallet.screens.destinations.NftDetailDestination
import app.eluvio.wallet.screens.destinations.PropertyDetailDestination
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.util.compose.focusRestorer
import app.eluvio.wallet.util.compose.thenIf
import app.eluvio.wallet.util.isKeyUpOf
import app.eluvio.wallet.util.rememberToaster
import app.eluvio.wallet.util.subscribeToState
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.roundToInt

@Composable
fun MyItems() {
    val toaster = rememberToaster()
    hiltViewModel<MyItemsViewModel>().subscribeToState(
        onEvent = {
            when (it) {
                is Events.NetworkError -> toaster.toast(it.defaultMessage)
                else -> {}
            }
        },
        onState = { vm, state ->
            MyItems(state, vm::onPropertySelected)
        }
    )
}

@Composable
private fun MyItems(
    state: MyItemsViewModel.State,
    onPropertySelected: (MyItemsViewModel.State.PropertyInfo?) -> Unit
) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.allMedia.loading) {
            EluvioLoadingSpinner()
        } else if (state.allMedia.media.isEmpty()) {
            Text(stringResource(R.string.no_content_warning))
        } else {
            val navigator = LocalNavigator.current
            val toaster = rememberToaster()
            MyItemsGrid(
                state,
                onItemClick = {
                    if (it.propertyId != null) {
                        navigator(PropertyDetailDestination(it.propertyId).asPush())
                    } else if (it.tokenId == null) {
                        toaster.toast("NFT Packs not supported yet")
                    } else {
                        navigator(NftDetailDestination(it.contractAddress, it.tokenId).asPush())
                    }
                },
                onPropertySelected = onPropertySelected
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.MyItemsGrid(
    state: MyItemsViewModel.State,
    onItemClick: (AllMediaProvider.Media) -> Unit,
    onPropertySelected: (MyItemsViewModel.State.PropertyInfo?) -> Unit
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
    val scrollState = rememberTvLazyGridState()
    val scope = rememberCoroutineScope()
    val firstItemFocusRequester = remember { FocusRequester() }
    val searchBoxFocusRequester = remember { FocusRequester() }
    TvLazyVerticalGrid(
        state = scrollState,
        columns = TvGridCells.Fixed(columnCount),
        horizontalArrangement = Arrangement.spacedBy(cardSpacing),
        verticalArrangement = Arrangement.spacedBy(cardSpacing),
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        pivotOffsets = PivotOffsets(0.1f),
        modifier = Modifier
            .focusRestorer(firstItemFocusRequester)
            .fillMaxSize()
            .onPreviewKeyEvent {
                if (it.isKeyUpOf(Key.Back) && scrollState.firstVisibleItemIndex > 0) {
                    scope.launch {
                        scrollState.animateScrollToItem(0)
                        searchBoxFocusRequester.requestFocus()
                    }
                    return@onPreviewKeyEvent true
                }
                false
            }
    ) {
        item(span = { TvGridItemSpan(maxLineSpan) }, contentType = "spacer") {
            Spacer(Modifier.height(10.dp))
        }
        item(span = { TvGridItemSpan(maxLineSpan) }, contentType = "search") {
            Column {
                SearchBox(
                    query = "",
                    hint = "Search My Items",
                    onQueryChanged = {},
                    onSearchClicked = {},
                    focusRequester = searchBoxFocusRequester
                )
                Spacer(Modifier.height(2.dp))
                HorizontalDivider()
                PropertyFilterRow(state, onPropertySelected)
            }
        }
        itemsIndexed(
            state.allMedia.media,
            key = { _, mediaItem -> mediaItem.key },
            contentType = { _, _ -> "media" }
        ) { index, mediaItem ->
            MediaCard(
                mediaItem,
                onClick = { onItemClick(mediaItem) },
                modifier = Modifier.thenIf(index == 0) {
                    focusRequester(firstItemFocusRequester)
                }
            )
        }
        item(span = { TvGridItemSpan(maxLineSpan) }, contentType = "spacer") {
            Spacer(Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PropertyFilterRow(
    state: MyItemsViewModel.State,
    onPropertySelected: (MyItemsViewModel.State.PropertyInfo?) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = state.selectedProperty != null) {
        onPropertySelected(null)
    }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = Overscan.horizontalPadding, vertical = 22.dp),
        modifier = Modifier
            .requiredWidth(screenWidth)
            .focusRestorer()
    ) {
        items(state.properties) { it ->
            SearchFilterChip(
                title = it.name,
                value = it,
                selected = it.id == state.selectedProperty?.id,
                onClicked = { property -> onPropertySelected(property) },
                onHighlighted = { /*No Op*/ }
            )
        }
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun MyItemsPreview() = EluvioThemePreview {
    val items = listOf(
        AllMediaProvider.Media(
            "key",
            "contract_address",
            "https://x",
            "Single Token",
            "Special Edition",
            "desc",
            "1",
            1
        ),
        AllMediaProvider.Media(
            "key",
            "contract_address",
            "https://x",
            "Token Pack",
            "Pleab Edition",
            "desc",
            null,
            53
        )
    )
    MyItems(
        MyItemsViewModel.State(
            AllMediaProvider.State(
                loading = false,
                // create 10 copies of the original list
                (1..10).flatMap { items }.map { it.copy(key = UUID.randomUUID().toString()) }
            ),
            properties = listOf(
                MyItemsViewModel.State.PropertyInfo("Property 1", "id_1"),
                MyItemsViewModel.State.PropertyInfo("Property 2", "_2")
            ),
        ), onPropertySelected = {})
}

@Composable
@Preview(device = Devices.TV_720p)
private fun MyItemsPreviewLoading() = EluvioThemePreview {
    MyItems(MyItemsViewModel.State(AllMediaProvider.State(loading = true)), onPropertySelected = {})
}
