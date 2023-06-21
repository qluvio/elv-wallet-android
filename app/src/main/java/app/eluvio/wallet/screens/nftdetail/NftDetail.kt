package app.eluvio.wallet.screens.nftdetail

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import app.eluvio.wallet.data.entities.MediaCollectionEntity
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.destinations.VideoPlayerActivityDestination
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.body_32
import app.eluvio.wallet.theme.title_62
import app.eluvio.wallet.util.ui.subscribeToState
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList

@MainGraph
@Destination(navArgsDelegate = NftDetailArgs::class)
@Composable
fun NftDetail(navCallback: NavigationCallback) {
    hiltViewModel<NftDetailViewModel>().subscribeToState(navCallback) { vm, state ->
        NftDetail(state, navCallback)
    }
}

@Composable
private fun NftDetail(state: NftDetailViewModel.State, navCallback: NavigationCallback) {
    Column(Modifier.padding(32.dp)) {
        Text(state.title, style = MaterialTheme.typography.title_62)
        Spacer(Modifier.height(16.dp))
        Text(state.subtitle, style = MaterialTheme.typography.body_32)
        LazyColumn {
            items(state.collections) { collection ->
                Spacer(Modifier.height(16.dp))
                Text(collection.name, style = MaterialTheme.typography.body_32)
                MediaItems(
                    collection.media,
                    onMediaItemClick = { onMediaItemClick(it, navCallback) })
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun MediaItems(media: RealmList<MediaEntity>, onMediaItemClick: (MediaEntity) -> Unit) {
    TvLazyRow(contentPadding = PaddingValues(16.dp)) {
        items(media) { media ->
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
                    modifier = Modifier.size(150.dp)
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
    }
}

private fun onMediaItemClick(media: MediaEntity, navCallback: NavigationCallback) {
    when (media.mediaType) {
        MediaEntity.MEDIA_TYPE_VIDEO -> {
            navCallback(VideoPlayerActivityDestination(media.id).asPush())
        }

        else -> {}
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun NftDetailPreview() = EluvioThemePreview {
    NftDetail(NftDetailViewModel.State(
        title = "Superman",
        subtitle = """
            Superman Web3 Movie Experience includes:
            Immersive menus featuring Fortress of Solitude, Metropolis, and Lex Luthor’s Lair
            Superman The Movie (Theatrical version) • Hours of special features*
            Curated image galleries • Hidden digital easter eggs
            A Voucher Code** for DC3 Super Power Pack: Series Superman from DC NFT Marketplace
        """.trimIndent(),
        collections = listOf(
            MediaCollectionEntity().apply {
                name = "Movies"
                media = realmListOf(
                    MediaEntity().apply {
                        name = "Superman 1"
                        mediaType = MediaEntity.MEDIA_TYPE_VIDEO
                    },
                    MediaEntity().apply {
                        name = "Superman 2"
                        mediaType = MediaEntity.MEDIA_TYPE_VIDEO
                    },
                )
            },
            MediaCollectionEntity().apply {
                name = "Extras"
                media = realmListOf(
                    MediaEntity().apply {
                        name = "Superman 2052 Poster"
                        mediaType = MediaEntity.MEDIA_TYPE_IMAGE
                    },
                    MediaEntity().apply {
                        name = "Man of Steel Trailer"
                        mediaType = MediaEntity.MEDIA_TYPE_VIDEO
                    },
                )
            }
        )
    ), navCallback = { })
}
