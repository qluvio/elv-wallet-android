package app.eluvio.wallet.screens.qrdialogs.externalmedia

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.screens.common.EluvioLoadingSpinner
import app.eluvio.wallet.screens.common.FullscreenDialogStyle
import app.eluvio.wallet.screens.common.Overscan
import app.eluvio.wallet.screens.common.generateQrCodeBlocking
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.title_62
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@Destination(
    style = FullscreenDialogStyle::class,
    navArgsDelegate = ExternalMediaQrDialogNavArgs::class
)
@Composable
fun ExternalMediaQrDialog() {
    hiltViewModel<ExternalMediaQrDialogViewModel>().subscribeToState({ }) { vm, state ->
        ExternalMediaQrDialog(state)
    }
}

@Composable
private fun ExternalMediaQrDialog(state: ExternalMediaQrDialogViewModel.State) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            // Scrim broke? do it manually i guess
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(Overscan.defaultPadding())
    ) {
        if (state.error) {
            ErrorView()
        } else {
            QrView(state.qrCode)
        }
    }
}

@Composable
private fun QrView(qrCode: Bitmap?) {
    Text(
        text = "Point your camera to the QR Code below for content",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.title_62,
        modifier = Modifier.fillMaxWidth(0.5f),
    )
    Spacer(Modifier.size(20.dp))
    if (qrCode != null) {
        Image(
            bitmap = qrCode.asImageBitmap(),
            contentDescription = "QR Code",
        )
    } else {
        EluvioLoadingSpinner()
    }
}

@Composable
private fun ErrorView() {
    Text(
        text = "Unable to load QR Code. Please try again later.",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.title_62,
        modifier = Modifier.fillMaxWidth(0.5f),
    )
}

@Composable
@Preview(device = Devices.TV_720p)
private fun QrDialogPreviewSuccess() = EluvioThemePreview {
    ExternalMediaQrDialog(
        ExternalMediaQrDialogViewModel.State(generateQrCodeBlocking("http://cf.io"))
    )
}

@Composable
@Preview(device = Devices.TV_720p)
private fun QrDialogPreviewLoading() = EluvioThemePreview {
    ExternalMediaQrDialog(ExternalMediaQrDialogViewModel.State(null))
}

@Composable
@Preview(device = Devices.TV_720p)
private fun QrDialogPreviewError() = EluvioThemePreview {
    ExternalMediaQrDialog(ExternalMediaQrDialogViewModel.State(null, error = true))
}
