package app.eluvio.wallet.screens.common

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.Navigator
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.destinations.ImageGalleryDestination
import app.eluvio.wallet.screens.destinations.QrDialogDestination
import app.eluvio.wallet.screens.destinations.VideoPlayerActivityDestination
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.util.logging.Log

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MediaItemCard(
    media: MediaEntity,
    modifier: Modifier = Modifier,
    onMediaItemClick: (MediaEntity) -> Unit = defaultMediaItemClickHandler(LocalNavigator.current),
    cardHeight: Dp = 150.dp,
) {
    val width = remember {
        if (media.mediaType == MediaEntity.MEDIA_TYPE_VIDEO) {
            cardHeight * 16f / 9f
        } else {
            cardHeight
        }
    }
    ImageCard(
        imageUrl = media.image,
        title = media.name,
        unFocusedOverlay = {
            if (media.mediaType == MediaEntity.MEDIA_TYPE_VIDEO) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                )
            }
        },
        onClick = { onMediaItemClick(media) },
        modifier = modifier.size(width, cardHeight)
    )
}

/**
 * Navigates to the appropriate destination based on the media type.
 */
fun defaultMediaItemClickHandler(navigator: Navigator): (media: MediaEntity) -> Unit =
    { media ->
        when (media.mediaType) {
            MediaEntity.MEDIA_TYPE_VIDEO -> {
                navigator(VideoPlayerActivityDestination(media.id).asPush())
            }

            MediaEntity.MEDIA_TYPE_IMAGE,
            MediaEntity.MEDIA_TYPE_GALLERY -> {
                navigator(ImageGalleryDestination(media.id).asPush())
            }

            else -> {
                if (media.mediaFile.isNotEmpty() || media.mediaLinks.isNotEmpty()) {
                    navigator(QrDialogDestination(media.id).asPush())
                } else {
                    Log.w("Tried to open unsupported media with no links: $media")
                }
            }
        }
    }

@Preview(device = Devices.TV_720p)
@Composable
private fun MediaItemCardPreview() = EluvioThemePreview {
    MediaItemCard(media = MediaEntity().apply {
        id = "id"
        name = "NFT Media Item"
        mediaType = MediaEntity.MEDIA_TYPE_IMAGE
    })
}

@Preview(device = Devices.TV_720p)
@Composable
private fun VideoItemCardPreview() = EluvioThemePreview {
    MediaItemCard(media = MediaEntity().apply {
        id = "id"
        name = "NFT Media Item"
        mediaType = MediaEntity.MEDIA_TYPE_VIDEO
    })
}
