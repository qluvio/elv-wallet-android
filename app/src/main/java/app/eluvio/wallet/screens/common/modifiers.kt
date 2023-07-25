package app.eluvio.wallet.screens.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color

/**
 * Conditionally applies a dimming effect to the content of this modifier.
 */
fun Modifier.dimContent(dim: Boolean = true, color: Color = Color.Black.copy(alpha = 0.8f)) =
    drawWithCache {
        onDrawWithContent {
            drawContent()
            if (dim) {
                drawRect(color)
            }
        }
    }
