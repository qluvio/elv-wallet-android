package app.eluvio.wallet.screens.dashboard.myitems

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.Text
import app.eluvio.wallet.theme.EluvioThemePreview

@Composable
fun MyItems() {
    Text(text = "hi")
}

@Composable
@Preview(Devices.TV_720p)
private fun MyItemsPreview() = EluvioThemePreview {
    MyItems()
}
