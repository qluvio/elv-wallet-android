package app.eluvio.wallet.screens.signin.ory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.navigation.AuthFlowGraph
import app.eluvio.wallet.screens.common.AppLogo
import app.eluvio.wallet.screens.common.EluvioLoadingSpinner
import app.eluvio.wallet.screens.common.Overscan
import app.eluvio.wallet.screens.common.TvButton
import app.eluvio.wallet.screens.common.offsetAndFakeSize
import app.eluvio.wallet.screens.signin.common.LoginState
import app.eluvio.wallet.screens.signin.common.QrData
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.title_62
import app.eluvio.wallet.util.compose.requestInitialFocus
import app.eluvio.wallet.util.subscribeToState
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination

@AuthFlowGraph
@Destination(navArgsDelegate = OrySignInNavArgs::class)
@Composable
fun OrySignIn() {
    hiltViewModel<OrySignInViewModel>().subscribeToState { vm, state ->
        OrySignIn(state, onRequestNewToken = vm::requestNewToken)
    }
}

@Composable
private fun OrySignIn(state: LoginState, onRequestNewToken: () -> Unit) {
    AsyncImage(
        model = state.bgImageUrl,
        contentDescription = null,
        Modifier.fillMaxSize()
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        AppLogo(
            Modifier
                .align(Alignment.Start)
                .padding(Overscan.defaultPadding())
        )
        Text(
            "ORY SIGN IN",
            style = MaterialTheme.typography.title_62,
            modifier = Modifier.offsetAndFakeSize(yOffset = (-24).dp)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            if (state.loading) {
                EluvioLoadingSpinner()
            } else {
                QrData(state.qrCode, state.userCode)
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        TvButton(
            stringResource(R.string.request_new_code),
            onClick = onRequestNewToken,
            modifier = Modifier.requestInitialFocus()
        )
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun OrySignInPreview() = EluvioThemePreview {
    OrySignIn(LoginState(), onRequestNewToken = {})
}
