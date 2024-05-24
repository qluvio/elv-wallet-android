package app.eluvio.wallet.screens.signin

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.navigation.AuthFlowGraph
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.common.AppLogo
import app.eluvio.wallet.screens.common.EluvioLoadingSpinner
import app.eluvio.wallet.screens.common.Overscan
import app.eluvio.wallet.screens.common.TvButton
import app.eluvio.wallet.screens.common.offsetAndFakeSize
import app.eluvio.wallet.screens.common.requestOnce
import app.eluvio.wallet.screens.destinations.MetamaskSignInDestination
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.header_30
import app.eluvio.wallet.theme.title_62
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@AuthFlowGraph
@Destination
@Composable
fun SignIn() {
    hiltViewModel<SignInViewModel>().subscribeToState { vm, state ->
        SignIn(
            state,
            onRequestNewToken = vm::requestNewToken
        )
    }
}

@Composable
private fun SignIn(
    state: SignInViewModel.State,
    onRequestNewToken: (qrSize: Int) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        AppLogo(
            Modifier
                .align(Alignment.Start)
                .padding(Overscan.defaultPadding())
        )
        Text(
            text = "Scan QR Code",
            style = MaterialTheme.typography.title_62,
            modifier = Modifier.offsetAndFakeSize(yOffset = (-24).dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Scan the QR Code with your camera app or a QR code reader on your device to verify the code.",
            style = MaterialTheme.typography.header_30.copy(fontWeight = FontWeight.Thin),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.3f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        var size by remember { mutableStateOf(0.dp) }
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            size = maxHeight
            if (state.loading) {
                EluvioLoadingSpinner()
            } else {
                QrData(state)
            }
        }
        val sizePx = with(LocalDensity.current) { size.toPx().toInt() }
        LaunchedEffect(sizePx) {
            if (sizePx > 0) {
                onRequestNewToken(sizePx)
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Column(Modifier.width(IntrinsicSize.Max)) {
            Row {
                val focusRequester = remember { FocusRequester() }
                TvButton(
                    stringResource(R.string.request_new_code),
                    onClick = { onRequestNewToken(sizePx) },
                    contentPadding = PaddingValues(horizontal = 40.dp, vertical = 5.dp),
                    modifier = Modifier.focusRequester(focusRequester)
                )
                focusRequester.requestOnce()
                Spacer(modifier = Modifier.width(10.dp))
                val navigator = LocalNavigator.current
                TvButton(text = "Back",
                    onClick = { navigator(NavigationEvent.GoBack) })
            }
            Spacer(modifier = Modifier.height(6.dp))
            val navigator = LocalNavigator.current
            TvButton(
                text = stringResource(R.string.metamask_sign_on_button),
                onClick = { navigator(MetamaskSignInDestination.asPush()) },
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF7B7B7B)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun QrData(state: SignInViewModel.State) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = state.userCode ?: "",
            style = MaterialTheme.typography.title_62
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (state.qrCode != null) {
            Image(
                bitmap = state.qrCode.asImageBitmap(),
                contentDescription = "qr code",
            )
        }
    }
}

@Preview(device = Devices.TV_720p)
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
        ),
        onRequestNewToken = {},
    )
}
