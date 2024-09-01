package app.eluvio.wallet.screens.purchaseprompt

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.data.AspectRatio
import app.eluvio.wallet.data.entities.LiveVideoInfoEntity
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.v2.permissions.PermissionContext
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.screens.common.MediaItemCard
import app.eluvio.wallet.screens.common.ShimmerImage
import app.eluvio.wallet.screens.common.generateQrCodeBlocking
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.carousel_36
import app.eluvio.wallet.theme.carousel_48
import app.eluvio.wallet.theme.label_24
import app.eluvio.wallet.theme.title_62
import app.eluvio.wallet.util.compose.RealisticDevices
import app.eluvio.wallet.util.subscribeToState
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant

@MainGraph
@Destination(navArgsDelegate = PermissionContext::class)
@Composable
fun PurchasePrompt() {
    hiltViewModel<PurchasePromptViewModel>().subscribeToState { vm, state ->
        PurchasePrompt(state)
    }
}

@Composable
private fun PurchasePrompt(state: PurchasePromptViewModel.State) {
    if (state.bgImageUrl != null) {
        AsyncImage(
            model = state.bgImageUrl,
            contentScale = ContentScale.FillWidth,
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize()
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Sign In On Browser to Purchase",
            style = MaterialTheme.typography.title_62,
            modifier = Modifier.padding(bottom = 18.dp)
        )

        when {
            state.media != null -> MediaPurchaseCard(state.media, state.qrImage)
            state.itemPurchase != null -> ItemPurchaseCard(state.itemPurchase, state.qrImage)
            state.qrImage != null -> {
                Image(
                    bitmap = state.qrImage.asImageBitmap(), contentDescription = "QR Code",
                    modifier = Modifier
                        .height(250.dp)
                        .aspectRatio(1f)
                )
            }
        }
    }
}

@Composable
private fun ItemPurchaseCard(
    itemPurchase: PurchasePromptViewModel.State.ItemPurchase,
    qrImage: Bitmap?,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .height(290.dp)
                .aspectRatio(0.65f)
                .paint(
                    painterResource(id = R.drawable.item_card_bg),
                    contentScale = ContentScale.FillBounds
                )
                .padding(12.dp)
        ) {
            Spacer(Modifier.height(32.dp))
            ShimmerImage(
                model = itemPurchase.image, //TODO: add baseurl
                contentDescription = "NFT image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(2.dp)
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = itemPurchase.title,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.carousel_36
            )
            Spacer(modifier = Modifier.height(4.dp))
            itemPurchase.subtitle?.let {
                Text(text = it.uppercase(), style = MaterialTheme.typography.label_24)
            }
        }
        qrImage?.let {
            Image(
                bitmap = it.asImageBitmap(), contentDescription = "QR Code",
                modifier = Modifier
                    .height(230.dp)
                    .aspectRatio(1f)
            )
        }
    }
}

@Composable
private fun MediaPurchaseCard(media: MediaEntity, qrImage: Bitmap?) {
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        val cardHeight = 250.dp
        Column(modifier = Modifier.width(IntrinsicSize.Min)) {
            MediaItemCard(
                media,
                cardHeight = cardHeight,
                onMediaItemClick = { _, _ -> /*No-Op*/ },
                modifier = Modifier
                    // Card is just for show. Disable interaction.
                    .focusProperties { canFocus = false }
            )
            Spacer(Modifier.height(14.dp))
            Text(
                media.name,
                style = MaterialTheme.typography.carousel_48,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        qrImage?.let {
            Image(
                bitmap = it.asImageBitmap(), contentDescription = "QR Code",
                modifier = Modifier
                    .height(cardHeight)
                    .aspectRatio(1f)
            )
        }
    }

}

@Composable
@Preview(device = RealisticDevices.TV_720p)
private fun MediaEntityPurchasePromptPreview() = EluvioThemePreview {
    PurchasePrompt(
        PurchasePromptViewModel.State(
            media = MediaEntity().apply {
                id = "id"
                name = "NFT Media Item"
                mediaType = MediaEntity.MEDIA_TYPE_VIDEO
                imageAspectRatio = AspectRatio.WIDE
                liveVideoInfo = LiveVideoInfoEntity().apply {
                    startTime = RealmInstant.MIN
                    title = "Tenacious D"
                    subtitle = "The Grand Arena"
                    headers = realmListOf("8pm Central", "Stage D", "Lorem Ipsum", "Dolor Sit Amet")
                }
            },
            qrImage = generateQrCodeBlocking("http://eluv.io")
        )
    )
}

@Composable
@Preview(device = RealisticDevices.TV_720p)
private fun ItemPurchasePromptPreview() = EluvioThemePreview {
    PurchasePrompt(
        PurchasePromptViewModel.State(
            itemPurchase = PurchasePromptViewModel.State.ItemPurchase(
                title = "Item Purchase",
                subtitle = "Subtitle",
                image = "http://example.com/image.png"
            ),
            qrImage = generateQrCodeBlocking("http://eluv.io")
        )
    )
}

@Composable
@Preview(device = RealisticDevices.TV_720p)
private fun PagePurchasePromptPreview() = EluvioThemePreview {
    PurchasePrompt(
        PurchasePromptViewModel.State(
            qrImage = generateQrCodeBlocking("http://eluv.io")
        )
    )
}
