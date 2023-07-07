package app.eluvio.wallet.screens.qrdialog

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
        if (state.qrCode != null) {
            Text(
                text = "Point your camera to the QR Code below for content",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.title_62,
                modifier = Modifier.fillMaxWidth(0.5f),
            )
            Spacer(Modifier.size(20.dp))
            Image(
                bitmap = state.qrCode.asImageBitmap(),
                contentDescription = "QR Code",
            )
        }
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun QrDialogPreview() = EluvioThemePreview {
    val bitmap = BitmapFactory.decodeResource(
        LocalContext.current.resources,
        R.mipmap.ic_launcher
    )
    QrDialog(QrDialogViewModel.State(bitmap))
}
