package app.eluvio.wallet.screens.mediagallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Text
import app.eluvio.wallet.navigation.Screen
import app.eluvio.wallet.util.ui.subscribeToState

@Composable
fun MediaGallery(navCallback: (Screen) -> Unit) {
    hiltViewModel<MediaGalleryViewModel>().subscribeToState { vm, state ->
        MediaGallery(state)
    }
}

@Composable
private fun MediaGallery(state: MediaGalleryViewModel.State) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text(text = "MEDIA GALLERY!")
    }
}

@Composable
@Preview(device = Devices.TV_1080p)
private fun MediaGalleryPreview() {
    val state = MediaGalleryViewModel.State(0)
    MediaGallery(state)
}
