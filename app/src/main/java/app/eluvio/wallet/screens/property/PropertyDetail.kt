package app.eluvio.wallet.screens.property

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import app.eluvio.wallet.app.Events
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.util.rememberToaster
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

/**
 * See [DynamicPageLayout] for @Preview
 */
@MainGraph
@Destination(navArgsDelegate = PropertyDetailNavArgs::class)
@Composable
fun PropertyDetail() {
    val toaster = rememberToaster()
    hiltViewModel<PropertyDetailViewModel>().subscribeToState(
        onEvent = {
            when (it) {
                is Events.NetworkError -> toaster.toast(it.defaultMessage)
                else -> {}
            }
        },
        onState = { vm, state ->
            DynamicPageLayout(state)
        }
    )
}
