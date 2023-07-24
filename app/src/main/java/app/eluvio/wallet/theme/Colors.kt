@file:OptIn(ExperimentalTvMaterial3Api::class)

package app.eluvio.wallet.theme

import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.darkColorScheme


fun EluvioColorScheme(): ColorScheme {
    return darkColorScheme(
        surface = Color(0xFF3E3F40),
        onSurface = Color.White,
        inverseSurface = Color(0xFFD4D4D4),
        inverseOnSurface = Color.Black,
        surfaceVariant = Color.White,
        onSurfaceVariant = Color.Black,
        secondaryContainer = Color(0xFF626262),
        onSecondaryContainer = Color.White,
    )
}

val ColorScheme.redeemTagSurface: Color get() = Color(0xFFFFD541)
val ColorScheme.onRedeemTagSurface: Color get() = Color.Black
