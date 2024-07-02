package app.eluvio.wallet.screens.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.screens.common.ShimmerImage
import app.eluvio.wallet.util.compose.requestOnce
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@MainGraph
@Destination(navArgsDelegate = ImageGalleryNavArgs::class)
@Composable
fun ImageGallery() {
    hiltViewModel<ImageGalleryViewModel>().subscribeToState { vm, state ->
        ImageGallery(state)
    }
}

@Composable
private fun ImageGallery(state: ImageGalleryViewModel.State) {
    var selectedImage by remember { mutableStateOf(state.images.firstOrNull()) }
    selectedImage?.let { image ->
        ShimmerImage(
            model = image.url,
            contentDescription = image.name,
            Modifier.fillMaxSize()
        )
    }
    var focusRequestedOnce by remember { mutableStateOf(false) }

    TvLazyRow(
        contentPadding = PaddingValues(32.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxHeight()
    ) {
        itemsIndexed(state.images) { index, image ->
            var isFocused by remember { mutableStateOf(false) }
            if (isFocused) {
                selectedImage = image
            }
            val focusRequester = remember { FocusRequester() }
            Surface(
                onClick = { /*TODO*/ },
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                ),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.hasFocus }
                    .height(100.dp)
                    .then(
                        image.aspectRatio?.let {
                            Modifier.aspectRatio(it, matchHeightConstraintsFirst = true)
                        } ?: Modifier
                    )
            ) {
                // Don't show thumbnails when there's only 1 item in the gallery
                if (state.images.size > 1) {
                    val imageAlpha by remember { derivedStateOf { if (isFocused) 1f else 0.75f } }
                    ShimmerImage(
                        model = image.url,
                        contentDescription = image.name,
                        alpha = imageAlpha,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize()
                    )
                }
            }
            if (!focusRequestedOnce && index == 0) {
                focusRequestedOnce = true
                focusRequester.requestOnce()
            }
        }
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun ImageGalleryPreview() = EluvioThemePreview {
    ImageGallery(ImageGalleryViewModel.State())
}
