package app.eluvio.wallet.screens.nftdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.data.entities.MediaCollectionEntity
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.MediaSectionEntity
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.screens.common.MediaItemsRow
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.body_32
import app.eluvio.wallet.theme.title_62
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination
import io.realm.kotlin.ext.realmListOf

@MainGraph
@Destination(navArgsDelegate = NftDetailArgs::class)
@Composable
fun NftDetail() {
    hiltViewModel<NftDetailViewModel>().subscribeToState { vm, state ->
        NftDetail(state)
    }
}

@Composable
private fun NftDetail(state: NftDetailViewModel.State) {
    Column(Modifier.padding(32.dp)) {
        Text(state.title, style = MaterialTheme.typography.title_62)
        Spacer(Modifier.height(16.dp))
        Text(state.subtitle, style = MaterialTheme.typography.body_32)
        TvLazyColumn {
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
