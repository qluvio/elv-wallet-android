package app.eluvio.wallet.data.stores

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import app.eluvio.wallet.util.base58
import app.eluvio.wallet.util.sharedprefs.getValue
import app.eluvio.wallet.util.sharedprefs.nullableString
import app.eluvio.wallet.util.sharedprefs.setValue
import app.eluvio.wallet.util.sharedprefs.toRemovalNotifyingSharedPrefs
import com.frybits.rx.preferences.rx3.Rx3SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        "encrypted_token_store",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    ).toRemovalNotifyingSharedPrefs()
    private val rxPrefs = Rx3SharedPreferences.create(prefs)

    var idToken by prefs.nullableString("id_token")
    var accessToken by prefs.nullableString("access_token")
    var refreshToken by prefs.nullableString("refresh_token")

    var clusterToken by prefs.nullableString("cluster_token")

    private val rxFabricToken = rxPrefs.getString("id_token")
    var fabricToken by rxFabricToken

    var walletAddress by prefs.nullableString("wallet_address")
    val userId: String? get() = walletAddress?.let { "iusr${it.base58}" }

    val isLoggedIn: Boolean get() = fabricToken != null
    val loggedInObservable = rxFabricToken.asObservable().map { _ -> rxFabricToken.isSet }

    fun wipe() {
        prefs.edit { clear() }
    }
}
