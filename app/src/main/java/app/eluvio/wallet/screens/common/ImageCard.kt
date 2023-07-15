package app.eluvio.wallet.screens.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import app.eluvio.wallet.R
import app.eluvio.wallet.theme.LocalSurfaceScale
import app.eluvio.wallet.theme.body_32
import coil.compose.AsyncImage

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ImageCard(
    imageUrl: String,
    title: String,
    modifier: Modifier = Modifier,
    unFocusedOverlay: @Composable (BoxScope.() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusedBorder = Border(BorderStroke(2.dp, MaterialTheme.colorScheme.onSecondaryContainer))
    Surface(
        onClick = onClick,
        border = ClickableSurfaceDefaults.border(focusedBorder = focusedBorder),
        scale = LocalSurfaceScale.current,
        interactionSource = interactionSource,
        modifier = modifier
    ) {
        val parentScope = this
        AsyncImage(
            model = imageUrl,
            contentScale = ContentScale.Crop,
            contentDescription = title,
            placeholder = debugPlaceholder(R.drawable.elv_logo),
            modifier = modifier.align(Alignment.Center)
        )
        if (isFocused) {
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = modifier
                    .fillMaxSize()
                    // TODO: get this from theme
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 10.dp, vertical = 20.dp)
            ) {
                WrapContentText(
                    text = title,
                    style = MaterialTheme.typography.body_32,
                    // TODO: get this from theme
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            unFocusedOverlay?.invoke(parentScope)
        }
    }
}
