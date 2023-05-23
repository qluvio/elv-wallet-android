package app.eluvio.wallet.ui.signin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Text
import app.eluvio.wallet.data.Environment
import app.eluvio.wallet.navigation.Screen

@Composable
fun SignIn(navCallback: (Screen) -> Unit = {}) {
    val vm: SignInViewModel = hiltViewModel()
    vm.observeState().subscribeAsState(initial = null).value?.let { state ->
        SignIn(state)
    }
}


@Composable
private fun SignIn(state: SignInViewModel.State) {
    Text(text = stringResource(id = state.env))
}

@Preview
@Composable
private fun SignInPreview() {
    SignIn(SignInViewModel.State(Environment.Main.envName))
}