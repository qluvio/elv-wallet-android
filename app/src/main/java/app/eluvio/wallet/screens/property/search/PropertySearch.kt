package app.eluvio.wallet.screens.property.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import app.eluvio.wallet.R
import app.eluvio.wallet.data.entities.v2.SearchFiltersEntity
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.screens.common.EluvioLoadingSpinner
import app.eluvio.wallet.screens.common.ImageCard
import app.eluvio.wallet.screens.common.Overscan
import app.eluvio.wallet.screens.common.SearchBox
import app.eluvio.wallet.screens.common.SearchFilterChip
import app.eluvio.wallet.screens.common.TvButton
import app.eluvio.wallet.screens.common.WrapContentText
import app.eluvio.wallet.screens.property.DynamicPageLayout
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.title_62
import app.eluvio.wallet.util.subscribeToState
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination

@MainGraph
@Destination(navArgsDelegate = PropertySearchNavArgs::class)
@Composable
fun PropertySearch() {
    var query by rememberSaveable { mutableStateOf("") }
    hiltViewModel<PropertySearchViewModel>().subscribeToState(
        onState = { vm, state ->
            BackHandler { vm.onBackPressed() }
            PropertySearch(
                state,
                query,
                onQueryChanged = {
                    query = it
                    vm.onQueryChanged(it)
                },
                onPrimaryFilterSelected = vm::onPrimaryFilterSelected,
                onSecondaryFilterClick = vm::onSecondaryFilterClicked,
                onSearchClicked = vm::onSearchClicked
            )
        },
        onEvent = {
            when (it) {
                is ResetQueryEvent -> {
                    query = ""
                }
            }
        }
    )
}

@Composable
private fun PropertySearch(
    state: PropertySearchViewModel.State,
    query: String,
    onQueryChanged: (String) -> Unit,
    onPrimaryFilterSelected: (SearchFiltersEntity.AttributeValue?) -> Unit,
    onSecondaryFilterClick: (String?) -> Unit,
    onSearchClicked: () -> Unit,
) {
    val bgModifier = Modifier
        .fillMaxSize()
        .background(Brush.linearGradient(listOf(Color(0xFF16151F), Color(0xFF0C0C10))))
    if (state.loading) {
        LoadingSpinner(modifier = bgModifier)
        return
    }
    Column(modifier = bgModifier) {
        Header(state, query, onQueryChanged, onSearchClicked)
        if (state.selectedFilters != null) {
            SecondaryFilterSelector(
                state,
                onPrimaryFilterCleared = { onPrimaryFilterSelected(null) },
                onSecondaryFilterClick = onSecondaryFilterClick
            )
        }
        if (state.primaryFilter != null) {
            PrimaryFilterSelector(state, onPrimaryFilterSelected)
        } else if (state.loadingResults) {
            LoadingSpinner(
                Modifier
                    .fillMaxWidth()
                    .padding(56.dp)
            )
        } else {
            DynamicPageLayout(state = state.searchResults)
        }
    }
}

@Composable
private fun LoadingSpinner(modifier: Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        EluvioLoadingSpinner()
    }
}

@Composable
private fun PrimaryFilterSelector(
    state: PropertySearchViewModel.State,
    onPrimaryFilterSelected: (SearchFiltersEntity.AttributeValue) -> Unit
) {
    val items = state.primaryFilter?.values.orEmpty()
    val cardSpacing = 20.dp
    TvLazyVerticalGrid(
        columns = TvGridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(cardSpacing),
        verticalArrangement = Arrangement.spacedBy(cardSpacing),
        contentPadding = Overscan.defaultPadding(),
    ) {
        items(items) { item ->
            val tag = item.value
            ImageCard(
                imageUrl = "${state.baseUrl}${item.image}",
                contentDescription = tag,
                onClick = { onPrimaryFilterSelected(item) },
                modifier = Modifier.aspectRatio(16f / 9f),
                focusedOverlay = {
                    PrimaryFilterOverlay(text = tag)
                },
                unFocusedOverlay = {
                    if (item.image == null) {
                        //
                        PrimaryFilterOverlay(
                            text = tag,
                            Modifier
                                .border(
                                    2.dp,
                                    LocalContentColor.current,
                                    shape = MaterialTheme.shapes.medium
                                )
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun PrimaryFilterOverlay(text: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize() then modifier
    ) {
        WrapContentText(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.title_62,
            color = Color.White,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 20.dp)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SecondaryFilterSelector(
    state: PropertySearchViewModel.State,
    onPrimaryFilterCleared: () -> Unit,
    onSecondaryFilterClick: (String?) -> Unit,
) {
    val filter = state.selectedFilters ?: return
    val secondaryFilters = filter.secondaryFilterAttribute?.values.orEmpty()
    val filterCount = secondaryFilters.size

    val backButtonFocusRequester = remember { FocusRequester() }
    val filterFocusRequesters = remember(filterCount) {
        List(filterCount) { FocusRequester() }
    }
    // Made non-lazy to make sure the "back" button (primary filter) is always attached so we can
    // always call .requestFocus() without crashing.
    // The number of secondary filters should be low enough that this is not a performance issue.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 22.dp)
            .horizontalScroll(rememberScrollState())
            // "Manual" focusRestorer.
            // We need to do this because when the secondary filter is cleared (via hardware back
            // button), for a split second there will be 0 items in the search results, which will
            // cause the system to focus on this row and re-select the last-selected secondary
            // filter.
            .focusProperties {
                enter = {
                    val selectedFilter = state.selectedFilters.secondaryFilterValue
                    if (selectedFilter == null) {
                        backButtonFocusRequester
                    } else {
                        secondaryFilters
                            .indexOfFirst { it.value == selectedFilter }
                            .takeIf { it != -1 }
                            ?.let { index ->
                                filterFocusRequesters[index]
                            }
                            ?: FocusRequester.Default
                    }
                }
            }
            .focusGroup()
    ) {
        Spacer(Modifier.width(Overscan.horizontalPadding))

        Image(
            imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color(0xFF939393)),
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.size(12.dp, 30.dp)
        )
        TvButton(
            text = filter.primaryFilterValue,
            onClick = onPrimaryFilterCleared,
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            modifier = Modifier
                .focusRequester(backButtonFocusRequester)
                .padding(end = 5.dp)
                .onFocusChanged {
                    if (it.hasFocus) {
                        onSecondaryFilterClick(null)
                    }
                }
        )

        secondaryFilters.forEachIndexed { index, attributeValue ->
            SearchFilterChip(
                title = attributeValue.value,
                selected = filter.secondaryFilterValue == attributeValue.value,
                onSelected = onSecondaryFilterClick,
                modifier = Modifier
                    .focusRequester(filterFocusRequesters[index])
                    .padding(horizontal = 5.dp)
            )
        }
        Spacer(Modifier.width(Overscan.horizontalPadding))
    }
}

@Composable
private fun Header(
    state: PropertySearchViewModel.State,
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearchClicked: () -> Unit
) {
    val placeholder = if (LocalInspectionMode.current) {
        painterResource(id = R.drawable.elv_logo_bw)
    } else {
        null
    }
    Row(Modifier.padding(Overscan.defaultPadding(excludeBottom = true))) {
        AsyncImage(
            model = "${state.baseUrl}${state.headerLogo}",
            contentDescription = "Logo",
            placeholder = placeholder,
            modifier = Modifier.height(48.dp)
        )
        Spacer(Modifier.width(24.dp))
        Column {
            SearchBox(
                query,
                hint = "Search ${state.propertyName}",
                onQueryChanged,
                onSearchClicked,
            )
            Spacer(Modifier.height(2.dp))
            HorizontalDivider()
        }
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun PropertySearchPreview() = EluvioThemePreview {
    PropertySearch(
        PropertySearchViewModel.State(
            loading = false,
            propertyName = "FlixVerse"
        ),
        query = "",
        onPrimaryFilterSelected = {},
        onSecondaryFilterClick = {},
        onQueryChanged = {},
        onSearchClicked = {}
    )
}
