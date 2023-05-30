package app.eluvio.wallet.ui.signin

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.navigation.Screen
import app.eluvio.wallet.ui.util.subscribeToState

@Composable
fun SignIn(navCallback: (Screen) -> Unit = {}) {
    hiltViewModel<SignInViewModel>().subscribeToState { vm, state ->
        DisposableEffect(Unit) {
            val navigationEvents = vm.navigationEvents.subscribe(navCallback)
            onDispose { navigationEvents.dispose() }
        }
        SignIn(
            state,
            onRequestNewToken = { vm.requestNewToken() },
            navCallback
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SignIn(
    state: SignInViewModel.State,
    onRequestNewToken: () -> Unit,
    navCallback: (Screen) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.loading) {
//            CircularProgressIndicator() // is this non-tv material stuff?
            Text("Loading...")
        }
        if (state.qrCode != null) {
            Image(bitmap = state.qrCode.asImageBitmap(), contentDescription = "qr code")
        }
        if (state.url != null) {
            Text(text = state.url)
            Text(text = state.userCode ?: "")
        }
        Row {
            Button(onClick = { onRequestNewToken() }) {
                Text("Request New Code")
            }
            Button(onClick = { navCallback(Screen.EnvironmentSelection) }) {
                Text(text = "Cancel")
            }
        }
    }
}

@Preview
@Composable
private fun SignInPreview() {
    val bitmap = BitmapFactory.decodeResource(
        LocalContext.current.resources,
        R.mipmap.ic_launcher
    )
    SignIn(
        state = SignInViewModel.State(
            loading = false,
            qrCode = bitmap,
            userCode = "ABC-DEF",
            url = "https://eluv.io",
        ),
        onRequestNewToken = {},
        navCallback = {}
    )
}
