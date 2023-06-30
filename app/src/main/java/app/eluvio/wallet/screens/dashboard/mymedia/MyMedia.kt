package app.eluvio.wallet.screens.dashboard.mymedia

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import app.eluvio.wallet.navigation.DashboardTabsGraph
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.ui.MediaItemCard
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@DashboardTabsGraph
@Destination
@Composable
fun MyMedia(navCallback: NavigationCallback) {
    hiltViewModel<MyMediaViewModel>().subscribeToState(navCallback) { vm, state ->
        MyMedia(state, navCallback)
    }
}

@Composable
private fun MyMedia(state: MyMediaViewModel.State, navCallback: NavigationCallback) {
    TvLazyRow(contentPadding = PaddingValues(16.dp)) {
        items(state.mediaItems) { media ->
            MediaItemCard(media, navCallback)
        }
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun MyMediaPreview() = EluvioThemePreview {
    MyMedia(MyMediaViewModel.State(), navCallback = { })
}
