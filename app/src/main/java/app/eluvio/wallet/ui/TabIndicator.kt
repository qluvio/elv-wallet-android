package app.eluvio.wallet.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpRect
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.TabRowDefaults

@OptIn(ExperimentalTvMaterial3Api::class)
val EluvioTabIndicator: @Composable (selectedTabIndex: Int, tabPositions: List<DpRect>) -> Unit =
    @Composable { selectedTabIndex, tabPositions ->
        tabPositions.getOrNull(selectedTabIndex)?.let {
            TabRowDefaults.PillIndicator(currentTabPosition = it)
        }
    }
