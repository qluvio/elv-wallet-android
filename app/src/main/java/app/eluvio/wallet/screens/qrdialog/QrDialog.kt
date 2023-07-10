package app.eluvio.wallet.screens.qrdialog

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.screens.common.EluvioLoadingSpinner
import app.eluvio.wallet.screens.common.FullscreenDialogStyle
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.title_62
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@Destination(
    style = FullscreenDialogStyle::class,
    navArgsDelegate = QrDialogNavArgs::class
)
@Composable
fun QrDialog() {
    hiltViewModel<QrDialogViewModel>().subscribeToState({ }) { vm, state ->
        QrDialog(state)
    }
}

@Composable
private fun QrDialog(state: QrDialogViewModel.State) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
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
    val bitmap = BitmapFactory.decodeResource(
        LocalContext.current.resources,
        R.mipmap.ic_launcher
    )
    QrDialog(QrDialogViewModel.State(bitmap))
}

@Composable
@Preview(device = Devices.TV_720p)
private fun QrDialogPreviewLoading() = EluvioThemePreview {
    QrDialog(QrDialogViewModel.State(null))
}

@Composable
@Preview(device = Devices.TV_720p)
private fun QrDialogPreviewError() = EluvioThemePreview {
    QrDialog(QrDialogViewModel.State(null, error = true))
}
