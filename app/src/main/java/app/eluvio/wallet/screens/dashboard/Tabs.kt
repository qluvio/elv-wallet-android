package app.eluvio.wallet.screens.dashboard

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import app.eluvio.wallet.R
import app.eluvio.wallet.util.compose.icons.Eluvio
import app.eluvio.wallet.util.compose.icons.MyItems

enum class Tabs(
    @StringRes val title: Int,
    val icon: ImageVector
) {
    Discover(R.string.dashboard_tab_discover, Icons.Default.Home),
    MyItems(R.string.dashboard_tab_my_items, Icons.Eluvio.MyItems),
    Profile(R.string.dashboard_tab_profile, Icons.Default.AccountCircle),
    ;

    companion object {
        val NoAuthTabs = listOf(Discover)
        val AuthTabs = listOf(Discover, MyItems, Profile)
    }
}
