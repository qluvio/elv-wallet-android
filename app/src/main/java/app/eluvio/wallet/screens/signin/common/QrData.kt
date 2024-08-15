package app.eluvio.wallet.screens.signin.common

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.theme.title_62

@Composable
fun QrData(qrCode: Bitmap?, userCode: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = userCode ?: "",
            style = MaterialTheme.typography.title_62
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (qrCode != null) {
            Image(
                bitmap = qrCode.asImageBitmap(),
                contentDescription = "qr code",
            )
        }
    }
}
