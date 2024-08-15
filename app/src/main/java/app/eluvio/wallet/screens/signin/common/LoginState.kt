package app.eluvio.wallet.screens.signin.common

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable

@Immutable
data class LoginState(
    val loading: Boolean = true,
    val qrCode: Bitmap? = null,
    val userCode: String? = null,
    val bgImageUrl: String? = null,
    val logoUrl: String? = null,
)
