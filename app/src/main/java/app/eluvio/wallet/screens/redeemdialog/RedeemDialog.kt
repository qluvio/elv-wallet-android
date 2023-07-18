package app.eluvio.wallet.screens.redeemdialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.common.debugPlaceholder
import app.eluvio.wallet.screens.destinations.FulfillmentQrDialogDestination
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.body_32
import app.eluvio.wallet.theme.redeemTagSurface
import app.eluvio.wallet.theme.title_62
import app.eluvio.wallet.util.subscribeToState
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination

@MainGraph
@Destination(navArgsDelegate = RedeemDialogNavArgs::class)
@Composable
fun RedeemDialog() {
    hiltViewModel<RedeemDialogViewModel>().subscribeToState { vm, state ->
        RedeemDialog(state)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun RedeemDialog(state: RedeemDialogViewModel.State) {
    Box(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .fillMaxWidth(0.8f)
                .align(Alignment.Center)
        ) {
            if (!state.image.isNullOrEmpty()) {
                AsyncImage(
                    model = state.image,
                    contentDescription = state.title,
                    placeholder = debugPlaceholder(R.drawable.elv_logo),
                    modifier = Modifier.fillMaxHeight()
                )
            }
            Spacer(Modifier.width(40.dp))
            Column {
                Text(text = state.title, style = MaterialTheme.typography.title_62)
                Spacer(Modifier.height(10.dp))
                Row {
                    if (state.offerValid) {
                        Text(
                            text = "OFFER VALID",
                            style = MaterialTheme.typography.body_32,
                            color = MaterialTheme.colorScheme.redeemTagSurface
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(text = state.dateRange, style = MaterialTheme.typography.body_32)
                }
                Spacer(Modifier.height(40.dp))
                val navigator = LocalNavigator.current
                // Should be a Card, but TV-Card can't be disabled yet.
                Surface(
                    onClick = { navigator(FulfillmentQrDialogDestination(state.transaction!!).asPush()) },
                    enabled = state.offerStatus != RedeemDialogViewModel.State.Status.REDEEMING,
                ) {
                    val text = remember(state.offerStatus) {
                        when (state.offerStatus) {
                            RedeemDialogViewModel.State.Status.UNREDEEMED -> "Redeem Now"
                            RedeemDialogViewModel.State.Status.REDEEMED -> "View"
                            RedeemDialogViewModel.State.Status.REDEEMING -> "Redeeming..."
                        }
                    }
                    Text(text, Modifier.padding(10.dp))
                }
            }
        }
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun RedeemedOfferPreview() = EluvioThemePreview {
    RedeemDialog(
        RedeemDialogViewModel.State(
            "Nft reward offer #1",
            "http://foo",
            true,
            "January 1, 1970 - January 1, 2042"
        )
    )
}

@Composable
@Preview(device = Devices.TV_720p)
private fun UnRedeemedOfferPreview() = EluvioThemePreview {
    RedeemDialog(
        RedeemDialogViewModel.State(
            "Nft reward offer #1",
            null,
            false,
            "January 1, 1970 - January 1, 2042"
        )
    )
}
