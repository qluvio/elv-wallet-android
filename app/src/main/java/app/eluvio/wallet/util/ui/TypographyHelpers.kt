package app.eluvio.wallet.util.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.tv.material3.LocalContentColor

@Composable
fun TextStyle.withAlpha(alpha: Float) =
    copy(color = color.takeOrElse { LocalContentColor.current }.copy(alpha = alpha))
