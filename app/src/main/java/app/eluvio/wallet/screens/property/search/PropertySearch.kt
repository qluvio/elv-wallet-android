package app.eluvio.wallet.screens.property.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.LocalTextStyle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.screens.common.EluvioLoadingSpinner
import app.eluvio.wallet.screens.common.ImageCard
import app.eluvio.wallet.screens.common.Overscan
import app.eluvio.wallet.screens.common.WrapContentText
import app.eluvio.wallet.screens.property.DynamicPageLayout
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.carousel_48
import app.eluvio.wallet.theme.title_62
import app.eluvio.wallet.util.compose.KeyboardClosedHandler
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
                onPrimaryFilterSelected = vm::onPrimaryFilterSelected,
                onQueryChanged = {
                    query = it
                    vm.onQueryChanged(it)
                },
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
    state: PropertySearchViewModel.State, query: String,
    onPrimaryFilterSelected: (String) -> Unit,
    onQueryChanged: (String) -> Unit,
    onSearchClicked: () -> Unit,
) {
    val bgModifier = Modifier
        .fillMaxSize()
        .background(Brush.linearGradient(listOf(Color(0xFF16151F), Color(0xFF0C0C10))))
        .padding(Overscan.defaultPadding())
    if (state.loading) {
        LoadingSpinner(modifier = bgModifier)
        return
    }
    Column(modifier = bgModifier) {
        Header(state, query, onQueryChanged, onSearchClicked)
        if (state.primaryFilters != null) {
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
    onPrimaryFilterSelected: (String) -> Unit
) {
    val items = state.primaryFilters.orEmpty()
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
                onClick = { onPrimaryFilterSelected(tag) },
                modifier = Modifier.aspectRatio(16f / 9f),
                focusedOverlay = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        WrapContentText(
                            text = tag,
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
            )
        }
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
    Row {
        AsyncImage(
            model = "${state.baseUrl}${state.headerLogo}",
            contentDescription = "Logo",
            placeholder = placeholder,
            modifier = Modifier.height(48.dp)
        )
        Spacer(Modifier.width(24.dp))
        Column {
            SearchBox(state, query, onQueryChanged, onSearchClicked)
            Spacer(Modifier.height(2.dp))
            HorizontalDivider()
        }
    }
}

@Composable
private fun SearchBox(
    state: PropertySearchViewModel.State,
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearchClicked: () -> Unit
) {
    val (textFocusRequester, surfaceFocusRequester) = remember { FocusRequester.createRefs() }
    // Copied from TV Material Text, since BasicTextField is a non-TV component
    val textColor = LocalTextStyle.current.color.takeOrElse {
        LocalContentColor.current
    }

    KeyboardClosedHandler {
        // Prevents the user from having to press Back just to go from textview to Surface
        surfaceFocusRequester.requestFocus()
    }
    Surface(
        onClick = { textFocusRequester.requestFocus() },
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.Transparent
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(2.dp, textColor))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(surfaceFocusRequester)
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChanged,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = { onSearchClicked() }),
            textStyle = MaterialTheme.typography.carousel_48.copy(color = textColor),
            cursorBrush = SolidColor(LocalContentColor.current),
            decorationBox = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(contentAlignment = Alignment.CenterStart) {
                        it()
                        if (query.isEmpty()) {
                            Text(
                                "Search ${state.propertyName}",
                                modifier = Modifier.graphicsLayer { alpha = 0.6f })
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp)
                .focusRequester(textFocusRequester)
        )
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
        onQueryChanged = {},
        onSearchClicked = {}
    )
}
