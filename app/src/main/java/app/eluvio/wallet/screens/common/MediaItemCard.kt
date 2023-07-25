package app.eluvio.wallet.screens.common

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.Navigator
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.destinations.ExternalMediaQrDialogDestination
import app.eluvio.wallet.screens.destinations.ImageGalleryDestination
import app.eluvio.wallet.screens.destinations.VideoPlayerActivityDestination
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.body_32
import app.eluvio.wallet.util.logging.Log

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MediaItemCard(
    media: MediaEntity,
    modifier: Modifier = Modifier,
    imageUrl: String = media.image,
    onMediaItemClick: (MediaEntity) -> Unit = defaultMediaItemClickHandler(LocalNavigator.current),
    cardHeight: Dp = 150.dp,
    aspectRatio: Float = media.imageAspectRatio ?: MediaEntity.ASPECT_RATIO_SQUARE,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    ImageCard(
        imageUrl = imageUrl,
        contentDescription = media.name,
        shape = shape,
        focusedOverlay = {
            WrapContentText(
                text = media.name,
                style = MaterialTheme.typography.body_32,
                // TODO: get this from theme
                color = Color.White,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 10.dp, vertical = 20.dp)
            )
        },
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
        modifier = modifier
            .height(cardHeight)
            .aspectRatio(aspectRatio, matchHeightConstraintsFirst = true)
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
                    navigator(ExternalMediaQrDialogDestination(media.id).asPush())
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
