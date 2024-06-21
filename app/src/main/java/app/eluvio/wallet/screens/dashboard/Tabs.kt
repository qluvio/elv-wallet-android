package app.eluvio.wallet.screens.dashboard

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import app.eluvio.wallet.R

enum class Tabs(
    @StringRes val title: Int,
    val icon: ImageVector? = null
) {
    Discover(R.string.dashboard_tab_discover),
    MyItems(R.string.dashboard_tab_my_items),
    MyMedia(R.string.dashboard_tab_my_media),
    Profile(R.string.dashboard_tab_profile),
    ;

    companion object {
        val NoAuthTabs = listOf(Discover, Profile)
        val AuthTabs = listOf(Discover, MyItems, Profile)
    }
}
