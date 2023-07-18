@file:OptIn(ExperimentalTvMaterial3Api::class)

package app.eluvio.wallet.theme

import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.darkColorScheme


fun EluvioColorScheme(): ColorScheme {
    return darkColorScheme(
        surface = Color(0xFF1C1C1C),
        surfaceVariant = Color.White,
        onSurfaceVariant = Color.Black,
        secondaryContainer = Color(0xFF626262),
        onSecondaryContainer = Color.White,
    )
}

val ColorScheme.redeemTagSurface: Color get() = Color(0xFFFFD541)
val ColorScheme.onRedeemTagSurface: Color get() = Color.Black
