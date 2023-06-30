package app.eluvio.wallet.screens.gallery

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.ImmersiveList
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.body_32
import app.eluvio.wallet.util.subscribeToState
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination

@MainGraph
@Destination(navArgsDelegate = ImageGalleryNavArgs::class)
@Composable
fun ImageGallery(navCallback: NavigationCallback) {
    hiltViewModel<ImageGalleryViewModel>().subscribeToState(navCallback) { vm, state ->
        ImageGallery(state, navCallback)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ImageGallery(state: ImageGalleryViewModel.State, navCallback: NavigationCallback) {
    // TODO make first item focused on launch
    ImmersiveList(
        modifier = Modifier.fillMaxSize(),
        listAlignment = Alignment.BottomStart,
        background = { index, listHasFocus ->
            if (listHasFocus) {
                AnimatedContent(targetState = index) {
                    val image = state.images[index]
                    AsyncImage(
                        model = image.url,
                        contentDescription = image.name,
                        Modifier.fillMaxSize()
                    )
                }
            }
            Text(text = "Index=$index, listHasFocus=$listHasFocus")
        },
        list = {
            TvLazyRow(
                contentPadding = PaddingValues(32.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(state.images) { index, image ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()
                    Surface(
                        onClick = { /*TODO*/ },
                        interactionSource = interactionSource,
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .immersiveListItem(index)
                            .size(100.dp)
                    ) {
                        val imageAlpha by remember { derivedStateOf { if (isFocused) 1f else 0.5f } }
                        AsyncImage(
                            model = image.url,
                            contentDescription = image.name,
                            alpha = imageAlpha,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxSize()
                        )
                        if (isFocused) {
                            Text(
                                image.name,
                                style = MaterialTheme.typography.body_32,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
@Preview(device = Devices.TV_720p)
private fun ImageGalleryPreview() = EluvioThemePreview {
    ImageGallery(ImageGalleryViewModel.State(), navCallback = { })
}
