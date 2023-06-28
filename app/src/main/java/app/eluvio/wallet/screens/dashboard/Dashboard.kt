package app.eluvio.wallet.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import androidx.tv.foundation.ExperimentalTvFoundationApi
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import app.eluvio.wallet.navigation.DashboardTabsGraph
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.screens.NavGraphs
import app.eluvio.wallet.theme.header_30
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.ui.AppLogo
import app.eluvio.wallet.util.ui.EluvioTabIndicator
import app.eluvio.wallet.util.ui.FocusGroup
import app.eluvio.wallet.util.ui.subscribeToState
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navigate

@MainGraph(start = true)
@Destination
@Composable
fun Dashboard(navCallback: NavigationCallback) {
    hiltViewModel<DashboardViewModel>().subscribeToState(navCallback) { _, state ->
        Dashboard(state, navCallback)
    }
}

@Composable
private fun Dashboard(state: DashboardViewModel.State, navCallback: NavigationCallback) {
    val tabNavController = rememberNavController()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TopBar(onTabSelected = { tab ->
            tabNavController.navigate(tab.direction) {
                launchSingleTop = true
            }
        }, navCallback)
        DestinationsNavHost(
            navGraph = NavGraphs.dashboardTabsGraph,
            navController = tabNavController,
            dependenciesContainerBuilder = { dependency(navCallback) },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalTvFoundationApi::class)
@Composable
private fun TopBar(onTabSelected: (Tabs) -> Unit, navCallback: NavigationCallback) {
    // TODO: there's nothing stopping the logo and tabs from overlapping if the screen isn't wide enough
    FocusGroup(contentAlignment = Alignment.Center, modifier = Modifier.onPreviewKeyEvent {
        // Exit screen when back is pressed while FocusGroup is focused
        if (it.key == Key.Back && it.type == KeyEventType.KeyUp) {
            navCallback(NavigationEvent.GoBack)
            return@onPreviewKeyEvent true
        }
        false
    }) {
        val focusManager = LocalFocusManager.current
        var selectedTabIndex by remember { mutableIntStateOf(0) }
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
                Tabs.values().forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onFocus = {
                            selectedTabIndex = index
                            onTabSelected(tab)
                            Log.e("Tab focused: $tab")
                        },
                        onClick = { focusManager.moveFocus(FocusDirection.Down) },
                        modifier = Modifier
                            .padding(10.dp)
                            .restorableFocus(),
                    ) {
                        val icon = tab.icon
                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = stringResource(tab.title)
                            )
                        } else {
                            Text(
                                text = stringResource(tab.title),
                                style = MaterialTheme.typography.header_30,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        }
    }
}

@DashboardTabsGraph
@Destination
@Composable
fun MyMedia() {
    Temp(tab = Tabs.MyMedia)
}

@DashboardTabsGraph
@Destination
@Composable
fun Search() {
    Temp(tab = Tabs.MyMedia)
}

@Composable
fun Temp(tab: Tabs) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text(text = "Welcome to ${stringResource(id = tab.title)}")
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun DashboardPreview() {
    val state = DashboardViewModel.State()
    Dashboard(state, navCallback = { })
}

@Composable
@Preview
private fun TopBarPreview() {
    TopBar(onTabSelected = { }, navCallback = { })
}
