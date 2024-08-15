package app.eluvio.wallet.screens.signin.auth0

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import app.eluvio.wallet.screens.destinations.MetamaskSignInDestination
import app.eluvio.wallet.screens.signin.common.LoginState
import app.eluvio.wallet.screens.signin.common.QrData
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.header_30
import app.eluvio.wallet.theme.title_62
import app.eluvio.wallet.util.compose.requestOnce
import app.eluvio.wallet.util.subscribeToState
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination

@AuthFlowGraph
@Destination(navArgsDelegate = Auth0SignInNavArgs::class)
@Composable
fun Auth0SignIn() {
    hiltViewModel<Auth0SignInViewModel>().subscribeToState { vm, state ->
        Auth0SignIn(
            state,
            onRequestNewToken = vm::requestNewToken
        )
    }
}

@Composable
private fun Auth0SignIn(state: LoginState, onRequestNewToken: () -> Unit) {
    AsyncImage(
        model = state.bgImageUrl,
        contentDescription = null,
        Modifier.fillMaxSize()
    )
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
        Column(Modifier.width(IntrinsicSize.Max)) {
            Row {
                val focusRequester = remember { FocusRequester() }
                TvButton(
                    stringResource(R.string.request_new_code),
                    onClick = onRequestNewToken,
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

@Preview(device = Devices.TV_720p)
@Composable
private fun Auth0SignInPreview() = EluvioThemePreview {
    val bitmap = BitmapFactory.decodeResource(
        LocalContext.current.resources,
        R.mipmap.ic_launcher
    )

    Auth0SignIn(
        state = LoginState(
            loading = false,
            qrCode = bitmap,
            userCode = "ABC-DEF",
        ),
        onRequestNewToken = {},
    )
}
