package app.eluvio.wallet.screens.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.destinations.ImageGalleryDestination
import app.eluvio.wallet.screens.destinations.QrDialogDestination
import app.eluvio.wallet.screens.destinations.VideoPlayerActivityDestination
import app.eluvio.wallet.theme.LocalSurfaceScale
import app.eluvio.wallet.theme.body_32
import app.eluvio.wallet.util.logging.Log
import coil.compose.AsyncImage

/**
 * A card that displays a media item. On click, it navigates to the appropriate destination.
 * For custom click handling, use the overload that doesn't take a [NavigationCallback].
 */
@Composable
fun MediaItemCard(
    media: MediaEntity,
    navCallback: NavigationCallback,
    modifier: Modifier = Modifier,
    cardHeight: Dp = 150.dp,
) {
    MediaItemCard(
        media = media,
        onMediaItemClick = defaultMediaItemClickHandler(navCallback),
        modifier = modifier,
        cardHeight = cardHeight,
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
@JvmName("MediaItemCardWithCustomOnClick")
fun MediaItemCard(
    media: MediaEntity,
    onMediaItemClick: (MediaEntity) -> Unit,
    modifier: Modifier = Modifier,
    cardHeight: Dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusedBorder = Border(BorderStroke(2.dp, MaterialTheme.colorScheme.onSecondaryContainer))
    val width = remember {
        if (media.mediaType == MediaEntity.MEDIA_TYPE_VIDEO) {
            cardHeight * 16f / 9f
        } else {
            cardHeight
        }
    }
    Surface(
        onClick = { onMediaItemClick(media) },
        border = ClickableSurfaceDefaults.border(focusedBorder = focusedBorder),
        scale = LocalSurfaceScale.current,
        interactionSource = interactionSource,
        modifier = modifier.size(width, cardHeight)
    ) {
        AsyncImage(
            model = media.image,
            contentScale = ContentScale.Crop,
            contentDescription = media.name,
            modifier = modifier
                .align(Alignment.Center)
        )
        if (isFocused) {
            Box(
                modifier
                    .fillMaxSize()
                    // TODO: get this from theme
                    .background(Color.Black.copy(alpha = 0.8f))
            ) {
                Text(
                    media.name,
                    style = MaterialTheme.typography.body_32,
                    // TODO: get this from theme
                    color = Color.White,
                    maxLines = 3,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(10.dp)
                )
            }
        } else if (media.mediaType == MediaEntity.MEDIA_TYPE_VIDEO) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

/**
 * Navigates to the appropriate destination based on the media type.
 */
fun defaultMediaItemClickHandler(navCallback: NavigationCallback): (media: MediaEntity) -> Unit =
    { media ->
        when (media.mediaType) {
            MediaEntity.MEDIA_TYPE_VIDEO -> {
                navCallback(VideoPlayerActivityDestination(media.id).asPush())
            }

            MediaEntity.MEDIA_TYPE_IMAGE,
            MediaEntity.MEDIA_TYPE_GALLERY -> {
                navCallback(ImageGalleryDestination(media.id).asPush())
            }

            else -> {
                if (media.mediaFile.isNotEmpty() || media.mediaLinks.isNotEmpty()) {
                    navCallback(QrDialogDestination(media.id).asPush())
                } else {
                    Log.w("Tried to open unsupported media with no links: $media")
                }
            }
        }
    }
