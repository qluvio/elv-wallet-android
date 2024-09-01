package app.eluvio.wallet.data.entities.v2.permissions

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

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
