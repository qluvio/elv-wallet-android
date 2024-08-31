package app.eluvio.wallet.data.entities.v2.permissions

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import app.eluvio.wallet.data.stores.Environment
import app.eluvio.wallet.util.crypto.Base58
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Represents the hierarchy for a specific item.
 * This is mostly needed for the purchase flow.
 */
@Parcelize
@Immutable
data class PermissionContext(
    val propertyId: String,
    val pageId: String? = null,
    val sectionId: String? = null,
    val sectionItemId: String? = null,
    val mediaItemId: String? = null,
) : Parcelable

/**
 * Creates a Wallet URL for either purchasing a specific it, or being offered multiple items that
 * will unlock access for this [PermissionContext].
 * The Wallet web client will figure out which permissions items are required according to the context.
 */
fun PermissionContext.toPurchaseUrl(environment: Environment): String {
    val context = JSONObject().apply {
        put("type", "purchase") // Always true?

        putOpt("id", sectionId)
        putOpt("id", sectionItemId) // Override the previous id, if it exists.

        putOpt("sectionSlugOrId", sectionId)
        putOpt("sectionItemId", sectionItemId)
    }.toString()
    val encodedContext = Base58.encode(context.toByteArray())
    return "${environment.walletUrl}/$propertyId/${pageId.orEmpty()}?p=$encodedContext"
}
