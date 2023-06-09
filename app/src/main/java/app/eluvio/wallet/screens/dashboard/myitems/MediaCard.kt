package app.eluvio.wallet.screens.dashboard.myitems

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.carousel_36
import app.eluvio.wallet.theme.label_24
import app.eluvio.wallet.util.logging.Log
import coil.compose.AsyncImage

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MediaCard(media: MyItemsViewModel.State.Media, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    Surface(
        onClick = { /*TODO*/ },
        interactionSource = interactionSource,
        modifier = modifier.aspectRatio(0.7f),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Header(media, focused)
            AsyncImage(
                model = media.imageUrl,
                contentDescription = "NFT image",
                placeholder = painterResource(id = R.drawable.elv_logo),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            Text(text = media.title, style = MaterialTheme.typography.carousel_36)
            Spacer(modifier = Modifier.height(4.dp))
            if (media.tokenCount > 1) {
                Text(
                    text = "View all ${media.tokenCount}",
                    style = MaterialTheme.typography.label_24,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.extraSmall
                        )
                        .padding(vertical = 2.dp, horizontal = 8.dp)
                )
            } else {
                media.subtitle?.let {
                    Text(text = it, style = MaterialTheme.typography.label_24)
                }
            }
        }
    }
}

@Composable
private fun Header(media: MyItemsViewModel.State.Media, focused: Boolean) {
    Row(Modifier.padding(12.dp)) {
        // add logo
        // add marketplace name
        Spacer(modifier = Modifier.weight(1f))
        val c = when (val current = LocalContentColor.current) {
            Color.Blue -> "blue"
            Color.Red -> "red"
            else -> current.toString()
        }
        media.tokenId?.let {
            Text(
                text = "#$it",
                style = MaterialTheme.typography.label_24
            )
        }
    }
}

@Composable
@Preview
fun MediaCardPreviewPack() = EluvioThemePreview {
    MediaCard(
        MyItemsViewModel.State.Media(
            "id1",
            "https://x",
            "Goat Pack",
            "Special Edition",
            null,
            23
        )
    )
}

@Composable
@Preview
fun MediaCardPreviewSingle() = EluvioThemePreview {
    MediaCard(
        MyItemsViewModel.State.Media(
            "id1",
            "https://x",
            "Single Token",
            "Special Edition",
            "1",
            1
        ),
    )
}
