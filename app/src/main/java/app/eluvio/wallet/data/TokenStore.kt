package app.eluvio.wallet.data

import android.content.Context
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

    var accountId by prefs.nullableString("account_id")
}
