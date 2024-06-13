package app.eluvio.wallet.screens.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.LocalSurfaceScale

/**
 * An image card with a focus border. Image is darkened when focused.
 */
@Composable
fun ImageCard(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    focusedOverlay: @Composable (BoxScope.() -> Unit)? = null,
    unFocusedOverlay: @Composable (BoxScope.() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusedBorder =
        Border(BorderStroke(2.dp, MaterialTheme.colorScheme.onSecondaryContainer), shape = shape)
    Surface(
        onClick = onClick,
        border = ClickableSurfaceDefaults.border(focusedBorder = focusedBorder),
        scale = LocalSurfaceScale.current,
        shape = ClickableSurfaceDefaults.shape(shape),
        interactionSource = interactionSource,
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent
        ),
        modifier = modifier
    ) {
        val parentScope = this
        ShimmerImage(
            model = imageUrl,
            contentScale = ContentScale.Crop,
            contentDescription = contentDescription,
            modifier = modifier
                .align(Alignment.Center)
                .dimContent(dim = isFocused)
        )
        if (isFocused) {
            focusedOverlay?.invoke(parentScope)
        } else {
            unFocusedOverlay?.invoke(parentScope)
        }
    }
}

@Preview(device = Devices.TV_720p)
@Composable
private fun ImageCardPreview() = EluvioThemePreview {
    ImageCard(imageUrl = "", contentDescription = "Card Title", onClick = {})
}
