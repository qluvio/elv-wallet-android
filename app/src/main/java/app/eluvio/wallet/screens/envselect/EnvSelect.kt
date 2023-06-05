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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.ExperimentalTvFoundationApi
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.data.Environment
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.navigation.Screens
import app.eluvio.wallet.navigation.asPushDestination
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.header_53
import app.eluvio.wallet.util.ui.EluvioTabIndicator
import app.eluvio.wallet.util.ui.FocusGroup
import app.eluvio.wallet.util.ui.subscribeToState
import app.eluvio.wallet.util.ui.withAlpha

@Composable
fun EnvSelect(navCallback: NavigationCallback) {
    hiltViewModel<EnvSelectViewModel>().subscribeToState(navCallback) { vm, state ->
        EnvironmentSelection(
            state = state,
            onEnvironmentSelected = { vm.selectEnvironment(it) },
            navCallback
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalTvFoundationApi::class)
@Composable
private fun EnvironmentSelection(
    state: EnvSelectViewModel.State,
    onEnvironmentSelected: (Environment) -> Unit,
    navCallback: NavigationCallback
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
                .copy(fontSize = 30.sp, fontWeight = FontWeight.Thin)
        )
        Text(
            text = "Media Wallet",
            style = MaterialTheme.typography.header_53.copy(fontSize = 50.sp)
        )

        FocusGroup(Modifier.onPreviewKeyEvent {
            // Exit screen when back is pressed while FocusGroup is focused
            if (it.key == Key.Back && it.type == KeyEventType.KeyUp) {
                navCallback(NavigationEvent.GoBack)
                return@onPreviewKeyEvent true
            }
            false
        }) {
            val selectedTabIndex = state.availableEnvironments.indexOf(state.selectedEnvironment)
            TabRow(
                selectedTabIndex = selectedTabIndex,
                contentColor = Color.White,
                indicator = { EluvioTabIndicator(selectedTabIndex, it) }
            ) {
                state.availableEnvironments.forEach { environment ->
                    val selected = state.selectedEnvironment == environment
                    val focusRequester = remember { FocusRequester() }
                    Tab(
                        selected = selected,
                        onFocus = { onEnvironmentSelected(environment) },
                        modifier = Modifier
                            .padding(10.dp)
                            .restorableFocus()
                            .focusRequester(focusRequester),
                    ) {
                        Text(stringResource(id = environment.envName))
                    }
                    LaunchedEffect(Unit) {
                        if (selected) {
                            focusRequester.requestFocus()
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Card(onClick = { navCallback(Screens.SignIn.asPushDestination()) }) {
            Text(stringResource(R.string.sign_in_button), Modifier.padding(10.dp))
        }
    }
}

@Preview(device = Devices.TV_720p)
@Composable
private fun EnvSelectPreview() = EluvioThemePreview {
    EnvironmentSelection(
        state = EnvSelectViewModel.State(
            false,
            Environment.values().toList(),
            Environment.Main
        ),
        onEnvironmentSelected = {},
        navCallback = { }
    )
}
