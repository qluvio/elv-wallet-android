package app.eluvio.wallet.network.dto.v2.permissions

import com.squareup.moshi.JsonClass

/**
 * Any object that can be permissioned. e.g. Properties, Pages, Sections, etc.
 */
interface DtoWithPermissions {
    val permissions: PermissionsDto?
}

@JsonClass(generateAdapter = true)
data class PermissionsDto(
    // Permission items required to access this object.
    val permission_item_ids: List<String>?,

    // Content permissions, trickles down to children.
    val behavior: String?,
    val alternate_page_id: String?,

    // Only applies to Pages
    val page_permissions: List<String>?,
    val page_permissions_behavior: String?,
    val page_permissions_alternate_page_id: String?,
)

@JsonClass(generateAdapter = true)
data class PermissionsStateDto(
    val authorized: Boolean,
)
