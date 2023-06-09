@file:OptIn(ExperimentalTvMaterial3Api::class)

package app.eluvio.wallet.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme

@Composable
fun EluvioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EluvioColorScheme(),
        shapes = MaterialTheme.shapes.copy(
            extraSmall = RoundedCornerShape(5.dp),
            small = RoundedCornerShape(16.dp),
            medium = RoundedCornerShape(5.dp),
            large = RoundedCornerShape(5.dp),
            extraLarge = RoundedCornerShape(5.dp),
        ),
        typography = EluvioTypography(),
        content = content
    )
}

@Composable
fun EluvioThemePreview(content: @Composable () -> Unit) {
    EluvioTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                content = content
            )
        }
    }
}
