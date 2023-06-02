package app.eluvio.wallet.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.foundation.ExperimentalTvFoundationApi
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.navigation.Screen
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.ui.AppLogo
import app.eluvio.wallet.util.ui.EluvioTabIndicator
import app.eluvio.wallet.util.ui.FocusGroup
import app.eluvio.wallet.util.ui.subscribeToState

@Composable
fun Dashboard(navCallback: NavigationCallback) {
    hiltViewModel<DashboardViewModel>().subscribeToState { vm, state ->
        Dashboard(state, navCallback)
    }
}

@Composable
private fun Dashboard(state: DashboardViewModel.State, navCallback: NavigationCallback) {
    val tabNavController = rememberNavController()
    Column {
        TopBar(tabNavController, navCallback)
        NavHost(
            navController = tabNavController,
            startDestination = tabs.first(),
            modifier = Modifier.fillMaxSize()
        ) {
            tabs.forEach { tab -> composable(tab) { Temp(tab) } }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalTvFoundationApi::class)
@Composable
private fun TopBar(tabNavController: NavController, navCallback: NavigationCallback) {
    // TODO: there's nothing stopping the logo and tabs from overlapping if the screen isn't wide enough
    FocusGroup(contentAlignment = Alignment.Center, modifier = Modifier.onPreviewKeyEvent {
        // Exit screen when back is pressed while FocusGroup is focused
        if (it.key == Key.Back && it.type == KeyEventType.KeyUp) {
            navCallback(Screen.GoBack)
            return@onPreviewKeyEvent true
        }
        false
    }) {
        val focusManager = LocalFocusManager.current
        var selectedTabIndex by remember { mutableStateOf(0) }
        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 30.dp)
        ) {
            AppLogo()
            val focusRequester = remember { FocusRequester() }
            TabRow(
                selectedTabIndex = selectedTabIndex,
                contentColor = Color.White,
                indicator = { EluvioTabIndicator(selectedTabIndex, it) },
                modifier = Modifier
                    .align(Alignment.Center)
                    .focusRequester(focusRequester)
            ) {
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onFocus = {
                            selectedTabIndex = index
                            tabNavController.navigate(tab)
                            Log.e("Tab focused: $tab")
                        },
                        onClick = { focusManager.moveFocus(FocusDirection.Down) },
                        modifier = Modifier
                            .padding(10.dp)
                            .restorableFocus(),
                    ) {
                        Text(text = tab)
                    }
                }
            }
        }
    }
}

@Composable
fun Temp(tab: String) {
    Text(text = "Welcome to $tab")
}

private val tabs = listOf("My Items", "My Media", "Profile", "Search")

@Composable
@Preview(device = Devices.TV_1080p)
private fun DashboardPreview() {
    val state = DashboardViewModel.State(0)
    Dashboard(state, NavigationCallback.NoOp)
}
