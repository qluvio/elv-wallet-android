package app.eluvio.wallet.screens.deeplink

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Text
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.screens.common.EluvioLoadingSpinner
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@MainGraph
@Destination(navArgsDelegate = SkuDetailsNavArgs::class)
@Composable
fun SkuDetails() {
    hiltViewModel<SkuDetailsViewModel>().subscribeToState { vm, state ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            SkuDetails(state)
        }
    }
}

@Composable
private fun SkuDetails(state: SkuDetailsViewModel.State) {
    when {
        state.loading -> EluvioLoadingSpinner()
        state.owned -> OwnedNftDetails(state)
        else -> UnownedNFTDetails(state)
    }
}

@Composable
fun UnownedNFTDetails(state: SkuDetailsViewModel.State) {
    Text("Go buy it bro")
}

@Composable
fun OwnedNftDetails(state: SkuDetailsViewModel.State) {
    Text("You got it, dude!")
}

@Composable
@Preview(device = Devices.TV_720p)
private fun SkuDetailsPreview() = EluvioThemePreview {
    SkuDetails(SkuDetailsViewModel.State())
}
