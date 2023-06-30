package app.eluvio.wallet.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.destinations.ImageGalleryDestination
import app.eluvio.wallet.screens.destinations.VideoPlayerActivityDestination
import app.eluvio.wallet.theme.body_32
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
    cardSize: DpSize = DpSize(150.dp, 150.dp),
) {
    MediaItemCard(
        media = media,
        onMediaItemClick = defaultMediaItemClickHandler(navCallback),
        modifier = modifier,
        cardSize = cardSize,
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
@JvmName("MediaItemCardWithCustomOnClick")
fun MediaItemCard(
    media: MediaEntity,
    onMediaItemClick: (MediaEntity) -> Unit,
    modifier: Modifier = Modifier,
    cardSize: DpSize = DpSize(150.dp, 150.dp),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    Surface(
        onClick = { onMediaItemClick(media) },
        interactionSource = interactionSource
    ) {
        Spacer(Modifier.width(16.dp))
        AsyncImage(
            model = media.image,
            contentDescription = media.name,
            modifier = modifier.size(cardSize)
        )
        if (isFocused) {
            Text(
                media.name,
                style = MaterialTheme.typography.body_32,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
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

            MediaEntity.MEDIA_TYPE_GALLERY -> {
                navCallback(ImageGalleryDestination(media.id).asPush())
            }

            else -> {}
        }
    }
