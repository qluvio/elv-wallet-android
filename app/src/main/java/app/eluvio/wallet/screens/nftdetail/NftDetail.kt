package app.eluvio.wallet.screens.nftdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.data.entities.MediaCollectionEntity
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.MediaSectionEntity
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.common.ImageCard
import app.eluvio.wallet.screens.common.MediaItemCard
import app.eluvio.wallet.screens.common.MediaItemsRow
import app.eluvio.wallet.screens.common.WrapContentText
import app.eluvio.wallet.screens.destinations.RedeemDialogDestination
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.body_32
import app.eluvio.wallet.theme.label_24
import app.eluvio.wallet.theme.onRedeemTagSurface
import app.eluvio.wallet.theme.redeemTagSurface
import app.eluvio.wallet.theme.title_62
import app.eluvio.wallet.util.subscribeToState
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import io.realm.kotlin.ext.realmListOf

@MainGraph
@Destination(navArgsDelegate = NftDetailArgs::class)
@Composable
fun NftDetail() {
    hiltViewModel<NftDetailViewModel>().subscribeToState { _, state ->
        NftDetail(state)
    }
}

@Composable
private fun NftDetail(state: NftDetailViewModel.State) {
    if (state.backgroundImage != null) {
        AsyncImage(
            model = state.backgroundImage,
            contentScale = ContentScale.Crop,
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize()
        )
    }
    Column(Modifier.padding(horizontal = 32.dp)) {
        TvLazyColumn {
            item {
                Spacer(Modifier.height(32.dp))
                Text(state.title, style = MaterialTheme.typography.title_62)
                Spacer(Modifier.height(16.dp))
                Text(state.subtitle, style = MaterialTheme.typography.body_32)
            }
            item {
                FeaturedMediaAndOffersRow(state)
            }

            state.sections.forEach { section ->
                section.name.takeIf { it.isNotEmpty() }?.let { sectionName ->
                    item(key = sectionName) {
                        Spacer(Modifier.height(16.dp))
                        Text(sectionName, style = MaterialTheme.typography.body_32)
                    }
                }
                items(section.collections) { collection ->
                    if (collection.name.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text(collection.name, style = MaterialTheme.typography.body_32)
                    }
                    MediaItemsRow(collection.media)
                }
            }
        }
    }
}

@Composable
private fun FeaturedMediaAndOffersRow(state: NftDetailViewModel.State) {
    TvLazyRow(
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(state.featuredMedia + state.redeemableOffers) { item ->
            val navigator = LocalNavigator.current
            when (item) {
                is MediaEntity -> MediaItemCard(item)
                is NftDetailViewModel.State.Offer -> OfferCard(item) {
                    navigator(
                        RedeemDialogDestination(
                            item.contractAddress,
                            item.tokenId,
                            item.offerId
                        ).asPush()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun OfferCard(item: NftDetailViewModel.State.Offer, onClick: () -> Unit) {
    // It's possible to layer this Text on top of the card (with explicit zIndex modifiers, see:
    // https://issuetracker.google.com/issues/291642442), but then it won't scale right when
    // the card is focused.
    // So instead we draw it both in the focused overlay, and unfocused overlay.
    val rewardText = remember<@Composable BoxScope.() -> Unit> {
        {
            Text(
                text = "REWARD",
                style = MaterialTheme.typography.label_24,
                color = MaterialTheme.colorScheme.onRedeemTagSurface,
                modifier = Modifier
                    .padding(10.dp)
                    .background(
                        MaterialTheme.colorScheme.redeemTagSurface,
                        MaterialTheme.shapes.extraSmall
                    )
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
    ImageCard(
        imageUrl = item.imageUrl,
        contentDescription = item.name,
        onClick = onClick,
        modifier = Modifier.size(150.dp),
        focusedOverlay = {
            WrapContentText(
                text = item.name,
                style = MaterialTheme.typography.body_32,
                // TODO: get this from theme
                color = Color.White,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 10.dp, vertical = 20.dp)
            )
            rewardText()
        },
        unFocusedOverlay = rewardText
    )
}

@Composable
@Preview(device = Devices.TV_720p)
private fun NftDetailPreview() = EluvioThemePreview {
    NftDetail(
        NftDetailViewModel.State(
            title = "Superman",
            subtitle = """
            Superman Web3 Movie Experience includes:
            Immersive menus featuring Fortress of Solitude, Metropolis, and Lex Luthor’s Lair
            Superman The Movie (Theatrical version) • Hours of special features*
            Curated image galleries • Hidden digital easter eggs
            A Voucher Code** for DC3 Super Power Pack: Series Superman from DC NFT Marketplace
        """.trimIndent(),
            featuredMedia = listOf(
                MediaEntity().apply {
                    name = "Feature Film"
                    mediaType = MediaEntity.MEDIA_TYPE_VIDEO
                },
            ),
            redeemableOffers = listOf(
                NftDetailViewModel.State.Offer(
                    "_id",
                    "NFT reward",
                    "https://via.placeholder.com/150",
                    "contractAddr",
                    "token_1"
                )
            ),
            sections = listOf(
                MediaSectionEntity().apply {
                    name = "Section 1"
                    collections = realmListOf(
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
                })
        )
    )
}

@Preview(widthDp = 200, heightDp = 200)
@Composable
private fun OfferCardPreview() = EluvioThemePreview {
    OfferCard(
        item = NftDetailViewModel.State.Offer(
            "_id",
            "NFT reward",
            "https://via.placeholder.com/150",
            "contractAddr",
            "token_1"
        ),
        onClick = {}
    )
}
