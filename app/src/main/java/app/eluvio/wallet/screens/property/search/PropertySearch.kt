package app.eluvio.wallet.screens.property.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
import app.eluvio.wallet.screens.common.Overscan
import app.eluvio.wallet.screens.common.TvButton
import app.eluvio.wallet.screens.property.DynamicPageLayout
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.button_24
import app.eluvio.wallet.theme.carousel_48
import app.eluvio.wallet.util.compose.requestInitialFocus
import app.eluvio.wallet.util.subscribeToState
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination

@MainGraph
@Destination(navArgsDelegate = PropertySearchNavArgs::class)
@Composable
fun PropertySearch() {
    hiltViewModel<PropertySearchViewModel>().subscribeToState { vm, state ->
        PropertySearch(state)
    }
}

@Composable
private fun PropertySearch(state: PropertySearchViewModel.State) {
    val bgModifier = Modifier
        .fillMaxSize()
        .background(Brush.linearGradient(listOf(Color(0xFF16151F), Color(0xFF0C0C10))))
        .padding(Overscan.defaultPadding())
    if (state.loading) {
        LoadingSpinner(modifier = bgModifier)
        return
    }
    Column(modifier = bgModifier) {
        Header(state)
        if (state.loadingResults) {
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
private fun Header(state: PropertySearchViewModel.State) {
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
            SearchBox(state)
            Spacer(Modifier.height(2.dp))
            HorizontalDivider()
        }
    }
}

@Composable
private fun SearchBox(state: PropertySearchViewModel.State) {
    var query by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(query) {
        state.onQueryChanged(query)
    }
    val textFocusRequester = remember { FocusRequester() }
    // Copied from TV Material Text, since BasicTextField is a non-TV component
    val textColor = LocalTextStyle.current.color.takeOrElse {
        LocalContentColor.current
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
            .onFocusChanged {
                if (it.hasFocus) {
                    textFocusRequester.requestFocus()
                }
            }
    ) {
        BasicTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = { state.onSearchClicked() }),
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
                .requestInitialFocus(textFocusRequester)
        )
    }
}

@Composable
private fun SearchBox_old(state: PropertySearchViewModel.State) {
    Column {
        Row {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier.size(24.dp)
            )
            Text("Search ${state.propertyName}")
        }
        Row {
            // 123
            // space
            // abc
            val btnSize = 30.dp
            CharRange('a', 'z').forEach {
                TvButton(
                    "$it",
                    textStyle = MaterialTheme.typography.button_24,
                    onClick = { /*TODO*/ },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(btnSize)
                )
            }
            TvButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Backspace",
                    modifier = Modifier.size(btnSize)
                )
            }
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
        )
    )
}
