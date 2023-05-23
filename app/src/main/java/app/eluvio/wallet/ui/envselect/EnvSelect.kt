package app.eluvio.wallet.ui.envselect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.ExperimentalTvFoundationApi
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.data.Environment
import app.eluvio.wallet.navigation.Screen
import app.eluvio.wallet.ui.util.FocusGroup
import app.eluvio.wallet.ui.util.onFocused

@Composable
fun EnvSelect(navCallback: (Screen) -> Unit = {}) {
    val vm: EnvSelectViewModel = hiltViewModel()
    vm.observeState().subscribeAsState(initial = null).value?.let { state ->
        EnvironmentSelection(
            state = state,
            onEnvironmentSelected = { vm.selectEnvironment(it) },
            navCallback
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalTvFoundationApi::class)
@Composable
fun EnvironmentSelection(
    state: EnvSelectViewModel.State,
    onEnvironmentSelected: (Environment) -> Unit,
    navCallback: (Screen) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        FocusGroup {
            TvLazyRow {
                items(state.availableEnvironments) { environment ->
                    Card(onClick = { /*no-op*/ },
                        Modifier
                            .padding(10.dp)
                            .restorableFocus()
                            .onFocused { onEnvironmentSelected(environment) }
                    ) {
                        Text(stringResource(id = environment.envName))
                    }
                }
            }
        }
        Card(onClick = { navCallback(Screen.SignIn) }, Modifier.padding(10.dp)) {
            Text(stringResource(R.string.sign_in_button))
        }
    }
}

@Preview(device = Devices.TV_1080p)
@Composable
private fun EnvSelectPreview(){
    EnvironmentSelection(
        state = EnvSelectViewModel.State(false, Environment.values().toList(), Environment.Main),
        onEnvironmentSelected = {},
        navCallback = {}
    )
}
