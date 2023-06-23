package app.eluvio.wallet.screens.gallery

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.ImmersiveList
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.body_32
import app.eluvio.wallet.util.ui.subscribeToState
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
    ImmersiveList(
        modifier = Modifier.fillMaxSize(),
        listAlignment = Alignment.BottomCenter,
        background = { index, listHasFocus ->
            Text(text = "Index=$index, listHasFocus=$listHasFocus")
        },
        list = {
            TvLazyRow(Modifier.padding(bottom = 32.dp)) {
                itemsIndexed(state.images) { index, image ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()
                    Surface(
                        onClick = { /*TODO*/ },
                        interactionSource = interactionSource,
                        modifier = Modifier.immersiveListItem(index)
                    ) {
                        AsyncImage(model = image.url, contentDescription = "todo")
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
