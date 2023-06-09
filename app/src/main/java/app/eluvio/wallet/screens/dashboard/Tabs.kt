package app.eluvio.wallet.screens.dashboard

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import app.eluvio.wallet.R

enum class Tabs(val route: String, @StringRes val title: Int, val icon: ImageVector? = null) {
    MyItems("tabs/my_items", R.string.dashboard_tab_my_items),
    MyMedia("tabs/my_media", R.string.dashboard_tab_my_media),
    Profile("tabs/profile", R.string.dashboard_tab_profile),
    Search("tabs/search", R.string.dashboard_tab_search, Icons.Default.Search),
    ;
}
