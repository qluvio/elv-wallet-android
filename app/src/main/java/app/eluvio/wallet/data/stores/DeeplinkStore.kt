package app.eluvio.wallet.data.stores

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import app.eluvio.wallet.util.boolean
import app.eluvio.wallet.util.crypto.Base58
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.nullableString
import dagger.hilt.android.qualifiers.ApplicationContext
import okio.ByteString.Companion.toByteString
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
    var installRefHandled by prefs.boolean("installRefHandled", false)
    var deeplinkRequest: DeeplinkRequest?
        get() {
            return DeeplinkRequest(
                contractId ?: return null,
                tokenId ?: return null
            )
        }
        set(value) {
            Log.d("Storing deeplink request: $value")
            contractId = value?.contractId
            tokenId = value?.tokenId
        }
    private var contractId by prefs.nullableString("contractId")
    private var tokenId by prefs.nullableString("tokenId")

    data class DeeplinkRequest(val contractId: String, val tokenId: String) {
        val contractAddress = contractId.removePrefix("ictr")
            .let { Base58.decode(it) }
            ?.toByteString()
            ?.hex()
            ?.let { "0x${it.removePrefix("0x")}" }
            ?: throw IllegalArgumentException("Invalid contractId: $contractId")
    }
}
