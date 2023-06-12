package app.eluvio.wallet.screens.nftdetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Text
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.util.ui.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@MainGraph
@Destination
@Composable
fun NftDetail(nftId: String, navCallback: NavigationCallback) {
    hiltViewModel<NftDetailViewModel>().subscribeToState(navCallback) { vm, state ->
        NftDetail(state, navCallback)
    }
}

@Composable
private fun NftDetail(state: NftDetailViewModel.State, navCallback: NavigationCallback) {
    Text("details for NFT: ${state.id}")
}

@Composable
@Preview(device = Devices.TV_720p)
private fun NftDetailPreview() = EluvioThemePreview {
    NftDetail(NftDetailViewModel.State(), navCallback = { })
}
