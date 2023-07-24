package app.eluvio.wallet.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import androidx.tv.foundation.ExperimentalTvFoundationApi
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.screens.NavGraphs
import app.eluvio.wallet.screens.common.AppLogo
import app.eluvio.wallet.screens.common.EluvioTab
import app.eluvio.wallet.screens.common.EluvioTabIndicator
import app.eluvio.wallet.screens.common.FocusGroup
import app.eluvio.wallet.screens.common.FocusGroupScope
import app.eluvio.wallet.screens.common.requestOnce
import app.eluvio.wallet.theme.header_30
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate

@MainGraph(start = true)
@Destination
@Composable
fun Dashboard() {
    hiltViewModel<DashboardViewModel>().subscribeToState { _, state ->
        Dashboard(state)
    }
}

@Composable
private fun Dashboard(state: DashboardViewModel.State) {
    val tabNavController = rememberNavController()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TopBar(onTabSelected = { tab ->
            tabNavController.navigate(tab.direction) {
                launchSingleTop = true
            }
        })
        val modifier = Modifier.fillMaxSize()
        if (LocalInspectionMode.current) {
            // Don't load real content in preview mode
            Text(
                text = "Dashboard page content",
                textAlign = TextAlign.Center,
                modifier = modifier.background(Color.Red.copy(alpha = 0.5f))
            )
        } else {
            DestinationsNavHost(
                navGraph = NavGraphs.dashboardTabsGraph,
                navController = tabNavController,
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalTvFoundationApi::class)
@Composable
private fun TopBar(onTabSelected: (Tabs) -> Unit) {
    val navigator = LocalNavigator.current
    // TODO: there's nothing stopping the logo and tabs from overlapping if the screen isn't wide enough
    FocusGroup(contentAlignment = Alignment.Center, modifier = Modifier.onPreviewKeyEvent {
        // Exit screen when back is pressed while FocusGroup is focused
        if (it.key == Key.Back && it.type == KeyEventType.KeyUp) {
            navigator(NavigationEvent.GoBack)
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
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(5.dp)
                    .align(Alignment.Center)
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    contentColor = Color.White,
                    indicator = { EluvioTabIndicator(selectedTabIndex, it) },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                ) {
                    focusRequester.requestOnce()
                    Tabs.values().forEachIndexed { index, tab ->
                        DashboardTab(
                            tab,
                            selected = selectedTabIndex == index,
                            onFocus = {
                                selectedTabIndex = index
                                onTabSelected(tab)
                                Log.e("Tab focused: $tab")
                            },
                            onClick = { focusManager.moveFocus(FocusDirection.Down) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvFoundationApi::class, ExperimentalTvMaterial3Api::class)
@Composable
private fun FocusGroupScope.DashboardTab(
    tab: Tabs,
    selected: Boolean,
    onFocus: () -> Unit,
    onClick: () -> Unit
) {
    EluvioTab(
        selected = selected,
        onFocus = onFocus,
        onClick = onClick,
        modifier = Modifier
            .padding(10.dp)
            .restorableFocus(),
    ) {
        val icon = tab.icon
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(tab.title),
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterVertically)
            )
        } else {
            Text(
                text = stringResource(tab.title),
                // TODO: use standard font
                style = MaterialTheme.typography.header_30.copy(fontWeight = FontWeight.W600),
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

//@DashboardTabsGraph
//@Destination
//@Composable
//fun Search() {
//    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
//        Text(text = "Welcome to ${stringResource(id = Tabs.Search.title)}")
//    }
//}

@Composable
@Preview(widthDp = 900)
private fun TopBarPreview() {
    TopBar(onTabSelected = { })
}

@Composable
@Preview(device = Devices.TV_720p)
private fun DashboardPreview() {
    val state = DashboardViewModel.State()
    Dashboard(state)
}
