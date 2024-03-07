package app.eluvio.wallet.data.stores

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import app.eluvio.wallet.util.boolean
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.nullableString
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeeplinkStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        "encrypted_deeplink_store",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Whether or not the install referrer data has been handled.
     */
    var installRefHandled by prefs.boolean("installRefHandled", false)

    private var marketplace by prefs.nullableString("marketplace")
    private var sku by prefs.nullableString("sku")
    private var jwt by prefs.nullableString("jwt")

    var deeplinkRequest: DeeplinkRequest?
        get() {
            return DeeplinkRequest(
                marketplace ?: return null,
                sku ?: return null,
                jwt ?: return null,
            )
        }
        set(value) {
            Log.d("Storing deeplink request: $value")
            marketplace = value?.marketplace
            sku = value?.sku
            jwt = value?.jwt
        }

    data class DeeplinkRequest(val marketplace: String, val sku: String, val jwt: String?)
}
