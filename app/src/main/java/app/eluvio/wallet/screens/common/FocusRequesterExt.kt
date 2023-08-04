package app.eluvio.wallet.screens.common

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay

/**
 * Requests focus as a LaunchedEffect once.
 */
@SuppressLint("ComposableNaming")
@Composable
inline fun FocusRequester.requestOnce(key: Any? = Unit) {
    LaunchedEffect(key) {
        requestFocus()
    }
}

fun Modifier.requestInitialFocus() = composed {
    val requester = remember { FocusRequester() }
    LaunchedEffect(requester) {
        // Delay 1ms to make sure this happens after composition
        delay(1)
        requester.requestFocus()
    }
    focusRequester(requester)
}
