package app.eluvio.wallet.screens.signin.metamask

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.screens.common.AppLogo
import app.eluvio.wallet.screens.common.EluvioLoadingSpinner
import app.eluvio.wallet.screens.common.TvButton
import app.eluvio.wallet.screens.signin.QrData
import app.eluvio.wallet.screens.signin.SignInViewModel
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.title_62
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@MainGraph
@Destination
@Composable
fun MetamaskSignIn() {
    hiltViewModel<MetamaskSignInViewModel>().subscribeToState { vm, state ->
        MetamaskSignIn(state, onRequestNewToken = vm::requestNewToken)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun MetamaskSignIn(state: SignInViewModel.State, onRequestNewToken: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        AppLogo(
            Modifier
                .align(Alignment.Start)
                .padding(start = 50.dp, top = 20.dp)
        )
        Text(
            stringResource(R.string.metamask_sign_on_title),
            style = MaterialTheme.typography.title_62
        )
        Image(
            painterResource(R.drawable.metamask_fox),
            contentDescription = "MetaMask Logo",
            modifier = Modifier.height(75.dp)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            if (state.loading) {
                EluvioLoadingSpinner()
            } else {
                QrData(state)
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        TvButton(stringResource(R.string.request_new_code), onClick = onRequestNewToken)
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun MetamaskSignInPreview() = EluvioThemePreview {
    MetamaskSignIn(SignInViewModel.State(), onRequestNewToken = {})
}
