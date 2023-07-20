package app.eluvio.wallet.screens.dashboard.mymedia

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import app.eluvio.wallet.navigation.DashboardTabsGraph
import app.eluvio.wallet.screens.common.MediaItemsRow
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@DashboardTabsGraph
@Destination
@Composable
fun MyMedia() {
    hiltViewModel<MyMediaViewModel>().subscribeToState { vm, state ->
        MyMedia(state)
    }
}

@Composable
private fun MyMedia(state: MyMediaViewModel.State) {
    // needs to be portrait aspect ratio
    MediaItemsRow(media = state.featuredMedia)
    // TODO add section media
}

@Composable
@Preview(device = Devices.TV_720p)
private fun MyMediaPreview() = EluvioThemePreview {
    MyMedia(MyMediaViewModel.State())
}
