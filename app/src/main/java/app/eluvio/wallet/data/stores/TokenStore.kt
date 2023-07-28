package app.eluvio.wallet.data.stores

import android.content.Context
import androidx.core.content.edit
import app.eluvio.wallet.util.base58
import app.eluvio.wallet.util.nullableString
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // todo: encrypt
    private val prefs = context.getSharedPreferences("token_store", Context.MODE_PRIVATE)

    var idToken by prefs.nullableString("id_token")
    var clusterToken by prefs.nullableString("cluster_token")
    var fabricToken by prefs.nullableString("id_token")
    var walletAddress by prefs.nullableString("wallet_address")
    val userId: String? get() = walletAddress?.let { "iusr${it.base58}" }

    fun wipe() {
        prefs.edit { clear() }
    }
}
