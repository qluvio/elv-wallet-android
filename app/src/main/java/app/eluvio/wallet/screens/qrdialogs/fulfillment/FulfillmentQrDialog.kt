package app.eluvio.wallet.screens.qrdialogs.fulfillment

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.screens.common.TvButton
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.carousel_48
import app.eluvio.wallet.theme.label_40
import app.eluvio.wallet.theme.title_62
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@MainGraph
@Destination(navArgsDelegate = FulfillmentQrDialogNavArgs::class)
@Composable
fun FulfillmentQrDialog() {
    hiltViewModel<FulfillmentQrDialogViewModel>().subscribeToState { vm, state ->
        if (state.code.isNotEmpty()) {
            // ignore empty state
            FulfillmentQrDialog(state)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun FulfillmentQrDialog(state: FulfillmentQrDialogViewModel.State) {
    Box(Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight()
                .align(Alignment.Center)
        ) {
            Text(text = "Success", style = MaterialTheme.typography.title_62)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Scan the QR Code with your camera app or a QR code reader on your device to claim your reward.",
                style = MaterialTheme.typography.label_40
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = state.code, style = MaterialTheme.typography.carousel_48)
            Spacer(modifier = Modifier.height(6.dp))
            if (state.qrBitmap != null) {
                Image(
                    bitmap = state.qrBitmap.asImageBitmap(),
                    contentDescription = "qr code",
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            val navigator = LocalNavigator.current
            TvButton(text = "Back", onClick = { navigator(NavigationEvent.GoBack) })
        }
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun FulfillmentQrDialogPreview() = EluvioThemePreview {
    FulfillmentQrDialog(FulfillmentQrDialogViewModel.State("1234567890"))
}
