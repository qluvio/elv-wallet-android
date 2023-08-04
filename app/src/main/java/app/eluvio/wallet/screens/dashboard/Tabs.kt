package app.eluvio.wallet.screens.dashboard

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import app.eluvio.wallet.R
import app.eluvio.wallet.screens.destinations.MyItemsDestination
import app.eluvio.wallet.screens.destinations.MyMediaDestination
import app.eluvio.wallet.screens.destinations.ProfileDestination
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec

// Order matters here. The order of the tabs in this enum is the order they will appear in the UI.
enum class Tabs(
    val direction: DirectionDestinationSpec,
    @StringRes val title: Int,
    val icon: ImageVector? = null
) {
    MyItems(MyItemsDestination, R.string.dashboard_tab_my_items),
    MyMedia(MyMediaDestination, R.string.dashboard_tab_my_media),
    Profile(ProfileDestination, R.string.dashboard_tab_profile),
//    Search(SearchDestination, R.string.dashboard_tab_search, Icons.Default.Search),
    ;
}
