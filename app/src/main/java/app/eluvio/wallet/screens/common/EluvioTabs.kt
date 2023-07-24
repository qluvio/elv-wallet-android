package app.eluvio.wallet.screens.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpRect
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Tab
import androidx.tv.material3.TabDefaults
import androidx.tv.material3.TabRowDefaults

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EluvioTab(
    selected: Boolean,
    onFocus: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Tab(
        selected = selected,
        onFocus = onFocus,
        onClick = onClick,
        colors = TabDefaults.pillIndicatorTabColors(
            contentColor = Color(0xFFD8D8D8),
            activeContentColor = Color(0xFFD8D8D8),
            selectedContentColor = Color.Black,
        ),
        modifier = modifier,
        content = content
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
val EluvioTabIndicator: @Composable (selectedTabIndex: Int, tabPositions: List<DpRect>) -> Unit =
    @Composable { selectedTabIndex, tabPositions ->
        tabPositions.getOrNull(selectedTabIndex)?.let {
            TabRowDefaults.PillIndicator(
                currentTabPosition = it,
                activeColor = Color(0xE5FFFFFF),
                inactiveColor = Color(0x99FFFFFF),
            )
        }
    }
