package app.eluvio.wallet.screens.dashboard.myitems

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.foundation.lazy.grid.rememberTvLazyGridState
import app.eluvio.wallet.app.Events
import app.eluvio.wallet.navigation.DashboardTabsGraph
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.util.ui.EluvioLoadingSpinner
import app.eluvio.wallet.util.ui.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination
import java.util.UUID

@DashboardTabsGraph(start = true)
@Destination
@Composable
fun MyItems(navCallback: NavigationCallback) {
    val context = LocalContext.current
    hiltViewModel<MyItemsViewModel>().subscribeToState(navCallback, onEvent = {
        when (it) {
            Events.NetworkError -> Toast.makeText(
                context,
                "Network error. Please try again later.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }) { vm, state ->
        MyItems(state, navCallback)
    }
}

@Composable
private fun MyItems(state: MyItemsViewModel.State, navCallback: NavigationCallback) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        if (state.loading) {
            EluvioLoadingSpinner(Modifier.fillMaxHeight())
        } else {
            val gridState = rememberTvLazyGridState()
            TvLazyVerticalGrid(
                columns = TvGridCells.Adaptive(240.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                state = gridState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 100.dp)
            ) {
                items(state.media, key = { it.id }) { media ->
                    MediaCard(media, Modifier.padding(10.dp))
                }
            }
        }
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun MyItemsPreview() = EluvioThemePreview {
    val items = listOf(
        MyItemsViewModel.State.Media(
            "id",
            "https://x",
            "Single Token",
            "Special Edition",
            "1",
            1
        ),
        MyItemsViewModel.State.Media(
            "id",
            "https://x",
            "Token Pack",
            "Pleab Edition",
            null,
            53
        )
    )
    MyItems(MyItemsViewModel.State(
        loading = false,
        // create 10 copies of the original list
        (1..10).flatMap { items }.map { it.copy(id = UUID.randomUUID().toString()) }
    ), navCallback = { })
}

@Composable
@Preview(device = Devices.TV_720p)
private fun MyItemsPreviewLoading() = EluvioThemePreview {
    MyItems(MyItemsViewModel.State(loading = true), navCallback = { })
}
