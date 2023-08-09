package app.eluvio.wallet.screens.envselect

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.ExperimentalTvFoundationApi
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.data.entities.SelectedEnvEntity
import app.eluvio.wallet.navigation.AuthFlowGraph
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.navigation.Navigator
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.common.EluvioTab
import app.eluvio.wallet.screens.common.EluvioTabIndicator
import app.eluvio.wallet.screens.common.FocusGroup
import app.eluvio.wallet.screens.common.TvButton
import app.eluvio.wallet.screens.common.requestInitialFocus
import app.eluvio.wallet.screens.common.requestOnce
import app.eluvio.wallet.screens.common.withAlpha
import app.eluvio.wallet.screens.destinations.SignInDestination
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.header_53
import app.eluvio.wallet.util.isKeyUpOf
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@AuthFlowGraph(start = true)
@Destination
@Composable
fun EnvSelect() {
    hiltViewModel<EnvSelectViewModel>().subscribeToState { vm, state ->
        EnvironmentSelection(
            state = state,
            onEnvironmentSelected = { vm.selectEnvironment(it) },
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun EnvironmentSelection(
    state: EnvSelectViewModel.State,
    onEnvironmentSelected: (SelectedEnvEntity.Environment) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.elv_logo),
            contentDescription = "Eluvio Logo"
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = "Welcome to",
            style = MaterialTheme.typography.header_53.withAlpha(0.8f)
                .copy(fontSize = 30.sp)
                .withAlpha(alpha = 0.4f)
        )
        Text(
            text = "Media Wallet",
            style = MaterialTheme.typography.header_53.copy(fontSize = 54.sp)
        )

        val navigator = LocalNavigator.current
        val hasMultipleEnvs by remember {
            derivedStateOf { state.availableEnvironments.size > 1 }
        }
        if (hasMultipleEnvs) {
            EnvironmentTabRow(state, navigator, onEnvironmentSelected)
        }
        Spacer(modifier = Modifier.height(20.dp))
        TvButton(
            stringResource(R.string.sign_in_button),
            onClick = { navigator(SignInDestination.asPush()) },
            modifier = if (hasMultipleEnvs) Modifier else Modifier.requestInitialFocus()
        )
    }
}

@OptIn(ExperimentalTvFoundationApi::class, ExperimentalTvMaterial3Api::class)
@Composable
fun EnvironmentTabRow(
    state: EnvSelectViewModel.State,
    navigator: Navigator,
    onEnvironmentSelected: (SelectedEnvEntity.Environment) -> Unit
) {
    val selectedTabIndex = state.availableEnvironments.indexOf(state.selectedEnvironment)
    FocusGroup(Modifier.onPreviewKeyEvent {
        // Exit screen when back is pressed while FocusGroup is focused
        if (it.isKeyUpOf(Key.Back)) {
            navigator(NavigationEvent.GoBack)
            return@onPreviewKeyEvent true
        }
        false
    }) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            contentColor = Color.White,
            indicator = { EluvioTabIndicator(selectedTabIndex, it) }
        ) {
            val tabFocusRequesters = remember {
                List(size = state.availableEnvironments.size, init = { FocusRequester() })
            }
            val focusManager = LocalFocusManager.current
            state.availableEnvironments.forEachIndexed { index, environment ->
                key(index) {
                    EluvioTab(
                        selected = index == selectedTabIndex,
                        onFocus = { onEnvironmentSelected(environment) },
                        onClick = { focusManager.moveFocus(FocusDirection.Down) },
                        modifier = Modifier
                            .padding(10.dp)
                            .restorableFocus()
                            .focusRequester(tabFocusRequesters[index]),
                    ) {
                        Text(stringResource(id = environment.prettyEnvName))
                    }
                }
            }
            if (selectedTabIndex != -1) {
                // Once we have a non-empty state (and only once), request focus on the selected tab
                tabFocusRequesters[selectedTabIndex].requestOnce()
            }
        }
    }
}

@Preview(device = Devices.TV_720p)
@Composable
private fun MultiEnvSelectPreview() = EluvioThemePreview {
    EnvironmentSelection(
        state = EnvSelectViewModel.State(
            false,
            SelectedEnvEntity.Environment.Main,
            SelectedEnvEntity.Environment.values().toList(),
        ),
        onEnvironmentSelected = {},
    )
}

@Preview(device = Devices.TV_720p)
@Composable
private fun SingleEnvSelectPreview() = EluvioThemePreview {
    EnvironmentSelection(
        state = EnvSelectViewModel.State(
            false,
            SelectedEnvEntity.Environment.Main,
            listOf(SelectedEnvEntity.Environment.Main),
        ),
        onEnvironmentSelected = {},
    )
}
