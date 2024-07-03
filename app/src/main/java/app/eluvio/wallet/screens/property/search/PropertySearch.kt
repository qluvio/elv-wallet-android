package app.eluvio.wallet.screens.property.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Text
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@MainGraph
@Destination(navArgsDelegate = PropertySearchNavArgs::class)
@Composable
fun PropertySearch() {
    hiltViewModel<PropertySearchViewModel>().subscribeToState { vm, state ->
        PropertySearch(state)
    }
}

@Composable
private fun PropertySearch(state: PropertySearchViewModel.State) {
    Text(text = "SEARCH")
}

@Composable
@Preview(device = Devices.TV_720p)
private fun PropertySearchPreview() = EluvioThemePreview {
    PropertySearch(PropertySearchViewModel.State())
}
