package app.eluvio.wallet.screens.signin

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.navigation.Screen
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.header_30
import app.eluvio.wallet.theme.label_40
import app.eluvio.wallet.theme.title_62
import app.eluvio.wallet.util.ui.subscribeToState

@Composable
fun SignIn(navCallback: NavigationCallback) {
    hiltViewModel<SignInViewModel>().subscribeToState { vm, state ->
        DisposableEffect(Unit) {
            val navigationEvents = vm.navigationEvents.subscribe { navCallback(it) }
            onDispose { navigationEvents.dispose() }
        }
        SignIn(
            state,
            onRequestNewToken = { vm.requestNewToken(it) },
            navCallback
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SignIn(
    state: SignInViewModel.State,
    onRequestNewToken: (qrSize: Int) -> Unit,
    navCallback: NavigationCallback
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        AppLogo()
        Text(text = "Scan QR Code", style = MaterialTheme.typography.title_62)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Scan the QR Code with your camera app or a QR code reader on your device to verify the code.",
            style = MaterialTheme.typography.header_30.copy(fontWeight = FontWeight.Thin),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.3f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (state.loading) {
//            CircularProgressIndicator() // is this non-tv material stuff?
        }
        Text(
            text = state.userCode ?: "",
            style = MaterialTheme.typography.title_62.copy(fontSize = 24.sp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        var size by remember { mutableStateOf(0.dp) }
        BoxWithConstraints(Modifier.weight(1f)) {
            size = maxHeight
            if (state.qrCode != null) {
                Image(
                    bitmap = state.qrCode.asImageBitmap(),
                    contentDescription = "qr code",
                )
            }
        }
        val sizePx = with(LocalDensity.current) { size.toPx().toInt() }
        LaunchedEffect(sizePx) {
            if (sizePx > 0) {
                onRequestNewToken(sizePx)
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row {
            val focusRequester = remember { FocusRequester() }
            Card(onClick = { onRequestNewToken(sizePx) }, Modifier.focusRequester(focusRequester)) {
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
                Text("Request New Code", Modifier.padding(10.dp))
            }
            Spacer(modifier = Modifier.width(35.dp))
            Card(onClick = { navCallback(Screen.GoBack) }) {
                Text(text = "Cancel", Modifier.padding(10.dp))
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun ColumnScope.AppLogo() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .align(Alignment.Start)
            .padding(start = 50.dp, top = 20.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.elv_logo),
            contentDescription = "Eluvio Logo",
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.label_40
        )
    }
}

@Preview(device = Devices.TV_1080p)
@Composable
private fun SignInPreview() = EluvioThemePreview {
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
        navCallback = NavigationCallback.NoOp
    )
}
