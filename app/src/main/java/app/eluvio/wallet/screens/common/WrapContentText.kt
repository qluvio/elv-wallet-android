package app.eluvio.wallet.screens.common

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.Text
import kotlin.math.ceil
import kotlin.math.max

/**
 * A [Text] composable that shrinks to fit its content.
 * To avoid wrapping inside a new Box, use [BoxWithConstraintsScope.WrapContentText].
 */
@Composable
fun WrapContentText(
    text: String,
    color: Color = Color.Unspecified,
    style: TextStyle,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
) {
    BoxWithConstraints {
        WrapContentText(text, color, style, maxLines, overflow, textAlign)
    }
}

/**
 * A [Text] composable that shrinks to fit its content.
 */
@Composable
fun BoxWithConstraintsScope.WrapContentText(
    text: String,
    color: Color = Color.Unspecified,
    style: TextStyle,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
) {
    val measurer = rememberTextMeasurer()
    val textWidthPx = measurer.measure(
        text,
        style = style,
        maxLines = maxLines,
        overflow = overflow,
        constraints = constraints
    ).maxLineWidth
    val widthDp = with(LocalDensity.current) { textWidthPx.toDp() }
    Text(
        text = text,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
        modifier = Modifier.width(widthDp)
    )
}

private val TextLayoutResult.maxLineWidth: Float
    get() {
        var maxWidth = 0f
        repeat(multiParagraph.lineCount) { line ->
            val lineWidth = multiParagraph.getLineWidth(line)
            maxWidth = max(maxWidth, lineWidth)
        }
        return ceil(maxWidth)
    }
