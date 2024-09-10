package app.eluvio.wallet.screens.signin.common

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import app.eluvio.wallet.data.FabricUrl

@Immutable
data class LoginState(
    val loading: Boolean = true,
    val qrCode: Bitmap? = null,
    val userCode: String? = null,
    val bgImageUrl: FabricUrl? = null,
    val logoUrl: FabricUrl? = null,
)
