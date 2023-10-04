@file:OptIn(ExperimentalTvMaterial3Api::class, ExperimentalTvMaterial3Api::class)

package app.eluvio.wallet.screens.deeplink

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.screens.common.EluvioLoadingSpinner
import app.eluvio.wallet.screens.common.TvButton
import app.eluvio.wallet.screens.common.requestOnce
import app.eluvio.wallet.screens.dashboard.myitems.MediaCard
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.body_32
import app.eluvio.wallet.theme.label_24
import app.eluvio.wallet.theme.title_62
import app.eluvio.wallet.util.subscribeToState
import com.ramcosta.composedestinations.annotation.Destination
import kotlin.math.roundToInt

@MainGraph
@Destination(navArgsDelegate = SkuDetailsNavArgs::class)
@Composable
fun SkuDetails() {
    hiltViewModel<SkuDetailsViewModel>().subscribeToState { vm, state ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            SkuDetails(state, vm::claimNft)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SkuDetails(state: SkuDetailsViewModel.State, onClaimClick: () -> Unit) {
    val media = state.media
    if (state.loading || media == null) {
        EluvioLoadingSpinner()
        return
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .fillMaxWidth(0.65f)
    ) {
        MediaCard(media, modifier = Modifier.width(210.dp))
        Spacer(Modifier.width(50.dp))
        Column(verticalArrangement = Arrangement.Center) {
            Text(text = media.title, style = MaterialTheme.typography.title_62)
            Spacer(modifier = Modifier.height(12.dp))
            //todo: timestamp goes here
            Text(text = media.description, style = MaterialTheme.typography.body_32)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val focusRequester = remember { FocusRequester() }
                val buttonText = if (state.claimingInProgress) {
                    val numberOfDots by animateDots()
                    "Activating${".".repeat(numberOfDots).padEnd(3, ' ')}"
                } else {
                    "Activate Pass"
                }
                TvButton(
                    text = buttonText,
                    onClick = onClaimClick,
                    modifier = Modifier.focusRequester(focusRequester),
                    scale = ClickableSurfaceDefaults.scale(
                        focusedScale = 1.0f,
                        pressedScale = 0.9f
                    ),
                    enabled = !state.claimingInProgress
                )
                focusRequester.requestOnce()
                Spacer(modifier = Modifier.width(8.dp))
                if (state.claimingInProgress) {
                    Text(
                        text = "This make take up to one minute.",
                        style = MaterialTheme.typography.label_24,
                        color = Color(0xFF959595)
                    )
                }
            }
        }
    }
}

@Composable
fun animateDots(): State<Int> {
    val floatAnimation by rememberInfiniteTransition(label = "ellipsis animation").animateFloat(
        initialValue = 1f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "ellipsis animation"
    )
    return remember { derivedStateOf { floatAnimation.roundToInt() } }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun SkuDetailsPreview() = EluvioThemePreview {
    SkuDetails(SkuDetailsViewModel.State(), onClaimClick = {})
}
