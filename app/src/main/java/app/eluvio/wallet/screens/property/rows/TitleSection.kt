package app.eluvio.wallet.screens.property.rows

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.screens.common.Overscan
import app.eluvio.wallet.screens.property.DynamicPageLayoutState
import app.eluvio.wallet.theme.body_32

@Composable
fun TitleSection(item: DynamicPageLayoutState.Section.Title) {
    Text(
        item.text,
        style = MaterialTheme.typography.body_32,
        modifier = Modifier.padding(
            horizontal = Overscan.horizontalPadding,
            vertical = 16.dp
        )
    )
}
