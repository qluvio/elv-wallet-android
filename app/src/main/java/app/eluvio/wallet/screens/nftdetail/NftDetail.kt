package app.eluvio.wallet.screens.nftdetail

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.common.DelayedFullscreenLoader
import app.eluvio.wallet.screens.common.TvButton
import app.eluvio.wallet.screens.dashboard.myitems.AllMediaProvider
import app.eluvio.wallet.screens.dashboard.myitems.MediaCard
import app.eluvio.wallet.screens.destinations.PropertyDetailDestination
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.body_32
import app.eluvio.wallet.theme.carousel_36
import app.eluvio.wallet.theme.label_24
import app.eluvio.wallet.util.compose.RealisticDevices
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@MainGraph
@Destination(navArgsDelegate = NftDetailNavArgs::class)
@Composable
fun NftDetail() {
    hiltViewModel<NftDetailViewModel>().subscribeToState { vm, state ->
        if (state.media != null) {
            NftDetail(state.media)
        } else {
            DelayedFullscreenLoader()
        }
    }
}

@Composable
private fun NftDetail(media: AllMediaProvider.Media) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(50.dp),
            modifier = Modifier.fillMaxSize(0.7f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MediaCard(media = media, onClick = null, modifier = Modifier.weight(1f))
                if (media.propertyId != null) {
                    val navigator = LocalNavigator.current
                    TvButton(
                        text = "Go to Property",
                        onClick = { navigator(PropertyDetailDestination(propertyId = media.propertyId).asPush()) },
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }

            NftMetadata(media)
        }
    }
}

@Composable
private fun MetadataTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.carousel_36.copy(
            fontWeight = FontWeight.Medium,
            lineHeight = 26.sp
        )
    )
}

private enum class NftTabs(val title: String) {
    DESCRIPTION("Description") {
        @Composable
        override fun Content(media: AllMediaProvider.Media) {
            Column {
                MetadataTitle(text = media.title)
                Text(
                    text = "${media.subtitle}    #${media.tokenId}",
                    style = MaterialTheme.typography.label_24,
                    color = Color(0xFF7a7a7a),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(text = media.description, style = MaterialTheme.typography.label_24)
            }
        }
    },
    MINT_INFO("Mint Info") {
        @Composable
        override fun Content(media: AllMediaProvider.Media) {
            Column {
                MetadataTitle("Media URL")
                Text("http...", Modifier.padding(bottom = 20.dp))

                MetadataTitle("Image URL")
                Text("http://..", Modifier.padding(bottom = 20.dp))

                Text(media.subtitle ?: "")
                Text("Number Minted: ###")
                Text("Number in Circulation: ###")
                Text("Number Burned: ###")
                Text("Maximum Possible in Circulation: ###")
                Text("Cap: ###")
            }
        }
    },
    CONTRACT_VERSION("Contract & Version") {
        @Composable
        override fun Content(media: AllMediaProvider.Media) {
            MetadataTitle("Contract Address")
            Text(media.contractAddress, Modifier.padding(bottom = 20.dp))

            MetadataTitle("Hash")
            Text("????")
        }
    };

    @Composable
    abstract fun Content(media: AllMediaProvider.Media)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun NftMetadata(media: AllMediaProvider.Media) {
    val tabs = NftTabs.entries
    var selectedTab by rememberSaveable { mutableStateOf(tabs[1]) }
//    var selectedTab by rememberSaveable { mutableStateOf(tabs.first()) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                10.dp,
                alignment = Alignment.CenterHorizontally
            ),
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth()
                .focusRestorer()
                .focusGroup()
        ) {
            tabs.forEach { tab ->
                MetadataTab(tab.title, selectedTab == tab, { selectedTab = tab })
            }
        }
        selectedTab.Content(media = media)
    }
}

@Composable
private fun MetadataTab(
    text: String,
    selected: Boolean,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    value: String? = text,
) {
    Surface(
        onClick = { onSelected(value) },
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = if (selected) Color.White else Color(0xFF7B7B7B),
            pressedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
        ),
        modifier = modifier
            .onFocusChanged {
                if (it.hasFocus) {
                    onSelected(value)
                }
            }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.body_32,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
@Preview(device = RealisticDevices.TV_720p)
private fun NftDetailPreview() = EluvioThemePreview {
    NftDetail(
        media = AllMediaProvider.Media(
            "key",
            "contract_address",
            "https://x",
            "Single Token",
            "Special Edition",
            "desc",
            "1",
            1,
            "tenant",
            "propertyId",
        )
    )
}
