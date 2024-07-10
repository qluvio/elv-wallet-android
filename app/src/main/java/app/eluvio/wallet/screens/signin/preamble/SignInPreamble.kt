package app.eluvio.wallet.screens.signin.preamble

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.ui.PlayerView
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.data.entities.SelectedEnvEntity
import app.eluvio.wallet.navigation.AuthFlowGraph
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.Navigator
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.common.EluvioTab
import app.eluvio.wallet.screens.common.EluvioTabIndicator
import app.eluvio.wallet.screens.common.TvButton
import app.eluvio.wallet.screens.destinations.SignInDestination
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.util.compose.requestInitialFocus
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@AuthFlowGraph(start = true)
@Destination
@Composable
fun SignInPreamble() {
    hiltViewModel<SignInPreambleViewModel>().subscribeToState { vm, state ->
        SignInPreamble(state, onEnvironmentSelected = vm::selectEnvironment)
    }
}

@Composable
private fun SignInPreamble(
    state: SignInPreambleViewModel.State,
    onEnvironmentSelected: (SelectedEnvEntity.Environment) -> Unit,
) {
    AnimatedBackground(state)
    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 40.dp, horizontal = 65.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.discover_logo),
                contentDescription = "Eluvio Logo",
                modifier = Modifier.fillMaxWidth(0.4f)
            )

            Spacer(Modifier.weight(1f))

            val navigator = LocalNavigator.current
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TvButton(
                    stringResource(R.string.sign_in_button),
                    onClick = { navigator(SignInDestination.asPush()) },
                    modifier = Modifier.requestInitialFocus()
                )
                val hasMultipleEnvs by remember {
                    derivedStateOf { state.availableEnvironments.size > 1 }
                }
                if (hasMultipleEnvs) {
                    Spacer(modifier = Modifier.height(10.dp))
                    EnvironmentTabRow(state, navigator, onEnvironmentSelected)
                }
            }
        }
    }
}

@Composable
private fun AnimatedBackground(state: SignInPreambleViewModel.State) {
    var lifecycle by remember { mutableStateOf(Lifecycle.Event.ON_CREATE) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            lifecycle = event
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                useController = false
            }
        },
        update = { playerView ->
            playerView.player = state.player
            when (lifecycle) {
                Lifecycle.Event.ON_PAUSE -> {
                    playerView.onPause()
                    playerView.player?.pause()
                }

                Lifecycle.Event.ON_RESUME -> {
                    playerView.onResume()
                    playerView.player?.play()
                }

                else -> {}
            }
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EnvironmentTabRow(
    state: SignInPreambleViewModel.State,
    navigator: Navigator,
    onEnvironmentSelected: (SelectedEnvEntity.Environment) -> Unit
) {
    val selectedTabIndex = state.availableEnvironments.indexOf(state.selectedEnvironment)
    val tabFocusRequesters = remember {
        List(size = state.availableEnvironments.size, init = { FocusRequester() })
    }
    TabRow(
        selectedTabIndex = selectedTabIndex,
        contentColor = Color.White,
        indicator = { tabPositions, doesTabRowHaveFocus ->
            EluvioTabIndicator(
                selectedTabIndex,
                tabPositions,
                doesTabRowHaveFocus
            )
        },
        modifier = Modifier.focusRestorer { tabFocusRequesters[selectedTabIndex] }
    ) {
        val focusManager = LocalFocusManager.current
        state.availableEnvironments.forEachIndexed { index, environment ->
            key(index) {
                EluvioTab(
                    selected = index == selectedTabIndex,
                    onFocus = { onEnvironmentSelected(environment) },
                    onClick = { focusManager.moveFocus(FocusDirection.Up) },
                    modifier = Modifier
                        .padding(10.dp)
                        .focusRequester(tabFocusRequesters[index]),
                ) {
                    Text(stringResource(id = environment.prettyEnvName))
                }
            }
        }
    }
}

@Preview(device = Devices.TV_720p)
@Composable
private fun MultiEnvSelectPreview() = EluvioThemePreview {
    SignInPreamble(
        state = SignInPreambleViewModel.State(
            false,
            SelectedEnvEntity.Environment.Main,
            SelectedEnvEntity.Environment.entries,
        ),
        onEnvironmentSelected = {},
    )
}

@Preview(device = Devices.TV_720p)
@Composable
private fun SingleEnvSelectPreview() = EluvioThemePreview {
    SignInPreamble(
        state = SignInPreambleViewModel.State(
            false,
            SelectedEnvEntity.Environment.Main,
            listOf(SelectedEnvEntity.Environment.Main),
        ),
        onEnvironmentSelected = {},
    )
}


@Composable
@Preview(device = Devices.TV_720p)
private fun SignInPreamblePreview() = EluvioThemePreview {
    SignInPreamble(SignInPreambleViewModel.State(), onEnvironmentSelected = {})
}


