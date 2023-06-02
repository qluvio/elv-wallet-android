package app.eluvio.wallet.screens.dashboard

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.eluvio.wallet.R

enum class Tabs(val route: String, @StringRes val title: Int, @DrawableRes val icon: Int? = null) {
    MyItems("tabs/my_items", R.string.dashboard_tab_my_items),
    MyMedia("tabs/my_media", R.string.dashboard_tab_my_media),
    Profile("tabs/profile", R.string.dashboard_tab_profile),
    Search("tabs/search", R.string.dashboard_tab_search, android.R.drawable.ic_menu_search),
    ;
}
