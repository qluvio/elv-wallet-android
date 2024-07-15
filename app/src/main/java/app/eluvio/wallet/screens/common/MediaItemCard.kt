package app.eluvio.wallet.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.data.entities.LiveVideoInfoEntity
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.MediaEntity.Companion.ASPECT_RATIO_WIDE
import app.eluvio.wallet.data.entities.getStartDateTimeString
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.Navigator
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.destinations.ExternalMediaQrDialogDestination
import app.eluvio.wallet.screens.destinations.ImageGalleryDestination
import app.eluvio.wallet.screens.destinations.LockedMediaDialogDestination
import app.eluvio.wallet.screens.destinations.VideoPlayerActivityDestination
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.body_32
import app.eluvio.wallet.theme.button_24
import app.eluvio.wallet.theme.label_24
import app.eluvio.wallet.theme.label_37
import app.eluvio.wallet.util.compose.requestInitialFocus
import app.eluvio.wallet.util.logging.Log
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant

@Composable
fun MediaItemCard(
    media: MediaEntity,
    modifier: Modifier = Modifier,
    imageUrl: String = media.imageOrLockedImage(),
    onMediaItemClick: (MediaEntity) -> Unit = defaultMediaItemClickHandler(LocalNavigator.current),
    cardHeight: Dp = 150.dp,
    aspectRatio: Float = media.aspectRatio(),
    shape: Shape = MaterialTheme.shapes.medium,
) {
    val liveVideoInfo = media.liveVideoInfo
    ImageCard(
        imageUrl = imageUrl,
        contentDescription = media.nameOrLockedName(),
        shape = shape,
        focusedOverlay = {
            if (liveVideoInfo != null) {
                LiveVideoFocusedOverlay(liveVideoInfo)
            } else {
                DefaultFocusedOverlay(media)
            }
        },
        unFocusedOverlay = {
            if (media.mediaType == MediaEntity.MEDIA_TYPE_VIDEO) {
                if (liveVideoInfo != null) {
                    LiveVideoUnFocusedOverlay(liveVideoInfo)
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center)
                            .alpha(0.75f)
                    )
                }
            }
        },
        onClick = { onMediaItemClick(media) },
        modifier = modifier
            .height(cardHeight)
            .aspectRatio(aspectRatio, matchHeightConstraintsFirst = true)
    )
}

@Composable
private fun BoxScope.DefaultFocusedOverlay(media: MediaEntity) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
        if (media.requireLockedState().locked) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .size(24.dp)
            )
        }
        WrapContentText(
            text = media.nameOrLockedName(),
            style = MaterialTheme.typography.body_32,
            // TODO: get this from theme
            color = Color.White,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 20.dp)
        )
    }
}

@Composable
private fun BoxScope.LiveVideoUnFocusedOverlay(liveVideo: LiveVideoInfoEntity) {
    when {
        liveVideo.ended -> {
            // Maybe never even displayed in the UI in the first place?
        }

        liveVideo.started -> {
            Text(
                "LIVE",
                style = MaterialTheme.typography.button_24,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier
                    .padding(15.dp)
                    .background(Color.Red, shape = RoundedCornerShape(2.dp))
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }

        else /* Upcoming */ -> {
            val startTime = liveVideo.getStartDateTimeString(LocalContext.current)
            Text(
                "UPCOMING\n$startTime",
                style = MaterialTheme.typography.button_24,
                textAlign = TextAlign.Center,
                color = Color(0xFFB3B3B3),
                modifier = Modifier
                    .padding(15.dp)
                    .background(Color(0xFF272727), shape = RoundedCornerShape(2.dp))
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
private fun BoxScope.LiveVideoFocusedOverlay(liveVideo: LiveVideoInfoEntity) {
    Column(
        Modifier
            .align(Alignment.BottomStart)
            .padding(23.dp)
    ) {
        val headers = liveVideo.headers.joinToString(separator = "   ")
        if (headers.isNotEmpty()) {
            Text(
                text = headers,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.label_24.copy(fontSize = 10.sp),
                color = Color(0xFFA5A6A8),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        liveVideo.title?.takeIf { it.isNotEmpty() }?.let { title ->
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.label_37.copy(fontSize = 14.sp),
            )
        }
        liveVideo.subtitle?.takeIf { it.isNotEmpty() }?.let { subtitle ->
            Text(
                text = subtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.label_24.copy(fontSize = 11.sp),
                color = Color(0xFF818590),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Navigates to the appropriate destination based on the media type.
 */
fun defaultMediaItemClickHandler(navigator: Navigator): (media: MediaEntity) -> Unit =
    { media ->
        if (media.requireLockedState().locked) {
            navigator(
                LockedMediaDialogDestination(
                    media.nameOrLockedName(),
                    media.imageOrLockedImage(),
                    media.requireLockedState().subtitle,
                    media.aspectRatio(),
                ).asPush()
            )
        } else {
            when (media.mediaType) {
                MediaEntity.MEDIA_TYPE_LIVE_VIDEO,
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
    }

@Preview(heightDp = 150, widthDp = 300)
@Composable
fun NonVideoCardPreview() = EluvioThemePreview {
    val media = MediaEntity().apply {
        id = "id"
        name = "NFT Media Item"
        mediaType = MediaEntity.MEDIA_TYPE_IMAGE
    }
    Row(modifier = Modifier.padding(10.dp)) {
        MediaItemCard(media)
        Spacer(modifier = Modifier.width(10.dp))
        MediaItemCard(media, modifier = Modifier.requestInitialFocus())
    }
}

@Preview(heightDp = 330, widthDp = 270)
@Composable
fun VideoCardPreview() = EluvioThemePreview {
    val media = MediaEntity().apply {
        id = "id"
        name = "NFT Media Item"
        mediaType = MediaEntity.MEDIA_TYPE_VIDEO
        imageAspectRatio = ASPECT_RATIO_WIDE
    }
    Column(modifier = Modifier.padding(10.dp)) {
        MediaItemCard(media)
        Spacer(modifier = Modifier.height(10.dp))
        MediaItemCard(media, modifier = Modifier.requestInitialFocus())
    }
}

@Preview(heightDp = 330, widthDp = 270)
@Composable
fun LiveVideoCardPreview() = EluvioThemePreview {
    val media = MediaEntity().apply {
        id = "id"
        name = "NFT Media Item"
        mediaType = MediaEntity.MEDIA_TYPE_VIDEO
        imageAspectRatio = ASPECT_RATIO_WIDE
        liveVideoInfo = LiveVideoInfoEntity().apply {
            startTime = RealmInstant.MIN
            title = "Tenacious D"
            subtitle = "The Grand Arena"
            headers = realmListOf("8pm Central", "Stage D", "Lorem Ipsum", "Dolor Sit Amet")
        }
    }
    Column(modifier = Modifier.padding(10.dp)) {
        MediaItemCard(media)
        Spacer(modifier = Modifier.height(10.dp))
        MediaItemCard(media, modifier = Modifier.requestInitialFocus())
    }
}

@Preview(heightDp = 330, widthDp = 270)
@Composable
fun UpcomingLiveVideoCardPreview() = EluvioThemePreview {
    val media = MediaEntity().apply {
        id = "id"
        name = "NFT Media Item"
        mediaType = MediaEntity.MEDIA_TYPE_VIDEO
        imageAspectRatio = ASPECT_RATIO_WIDE
        liveVideoInfo = LiveVideoInfoEntity().apply {
            startTime = RealmInstant.MAX
            title = "Tenacious D"
            subtitle = "The Grand Arena"
            headers = realmListOf("8pm Central", "Stage D", "Lorem Ipsum", "Dolor Sit Amet")
        }
    }
    Column(modifier = Modifier.padding(10.dp)) {
        MediaItemCard(media)
        Spacer(modifier = Modifier.height(10.dp))
        MediaItemCard(media, modifier = Modifier.requestInitialFocus())
    }
}

@Preview(heightDp = 150, widthDp = 270, locale = "fr")
@Composable
fun EndedLiveVideoCardPreview() = EluvioThemePreview {
    MediaItemCard(media = MediaEntity().apply {
        id = "id"
        name = "NFT Media Item"
        mediaType = MediaEntity.MEDIA_TYPE_VIDEO
        imageAspectRatio = ASPECT_RATIO_WIDE
        liveVideoInfo = LiveVideoInfoEntity().apply {
            endTime = RealmInstant.MIN
        }
    })
    Text("UNDEFINED DESIGN: TBD", modifier = Modifier.align(Alignment.Center))
}
