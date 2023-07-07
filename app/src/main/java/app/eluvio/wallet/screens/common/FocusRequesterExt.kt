package app.eluvio.wallet.screens.common

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester

/**
 * Requests focus as a LaunchedEffect once.
 */
@SuppressLint("ComposableNaming")
@Composable
inline fun FocusRequester.requestOnce() {
    LaunchedEffect(Unit) {
        requestFocus()
    }
}
