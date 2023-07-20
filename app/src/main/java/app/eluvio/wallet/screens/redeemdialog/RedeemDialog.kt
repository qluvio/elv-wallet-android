package app.eluvio.wallet.screens.redeemdialog

import android.widget.Toast
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import app.eluvio.wallet.app.Events
import app.eluvio.wallet.data.entities.RedeemStateEntity
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.screens.common.EluvioLoadingSpinner
import app.eluvio.wallet.screens.common.ShimmerImage
import app.eluvio.wallet.screens.common.VideoPlayer
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.body_32
import app.eluvio.wallet.theme.redeemTagSurface
import app.eluvio.wallet.theme.title_62
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination

@MainGraph
@Destination(navArgsDelegate = RedeemDialogNavArgs::class)
@Composable
fun RedeemDialog() {
    val context = LocalContext.current
    hiltViewModel<RedeemDialogViewModel>().subscribeToState(
        onEvent = {
            when (it) {
                Events.NetworkError -> Toast.makeText(
                    context,
                    "Network error. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        onState = { vm, state ->
            if (state.title.isNotEmpty()) {
                // ignore empty state
                RedeemDialog(state, onRedeemClicked = { vm.redeemOrShowOffer() })
            }
        }
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun RedeemDialog(state: RedeemDialogViewModel.State, onRedeemClicked: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .fillMaxWidth(0.8f)
                .align(Alignment.Center)
        ) {
            if (state.animation != null) {
                VideoPlayer(state.animation, modifier = Modifier.widthIn(max = 270.dp))
            } else if (!state.image.isNullOrEmpty()) {
                ShimmerImage(
                    model = state.image,
                    contentDescription = state.title,
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
                RedeemButton(state.offerStatus, onRedeemClicked)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
private fun RedeemButton(
    offerStatus: RedeemStateEntity.Status,
    onRedeemClicked: () -> Unit
) {
    val isRedeeming by remember(offerStatus) {
        derivedStateOf { offerStatus == RedeemStateEntity.Status.REDEEMING }
    }
    Row {
        // Should be a Card, but TV-Card can't be disabled yet.
        Surface(
            onClick = onRedeemClicked,
            enabled = !isRedeeming,
        ) {
            val text = remember(offerStatus) {
                when (offerStatus) {
                    RedeemStateEntity.Status.REDEEMED -> "View"
                    RedeemStateEntity.Status.REDEEMING -> "Redeeming..."
                    RedeemStateEntity.Status.REDEEM_FAILED,
                    RedeemStateEntity.Status.UNREDEEMED -> "Redeem Now"
                }
            }
            Text(text, Modifier.padding(10.dp))
        }
        if (isRedeeming) {
            Spacer(Modifier.width(16.dp))
            EluvioLoadingSpinner()
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
            null,
            true,
            "January 1, 1970 - January 1, 2042"
        ),
        onRedeemClicked = {}
    )
}

@Composable
@Preview(device = Devices.TV_720p)
private fun UnRedeemedOfferPreview() = EluvioThemePreview {
    RedeemDialog(
        RedeemDialogViewModel.State(
            "Nft reward offer #1",
            null,
            null,
            false,
            "January 1, 1970 - January 1, 2042"
        ),
        onRedeemClicked = {}
    )
}
