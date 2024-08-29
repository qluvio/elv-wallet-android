package app.eluvio.wallet.network.converters.v2.permissions

import app.eluvio.wallet.data.entities.v2.permissions.PermissionsEntity
import app.eluvio.wallet.network.dto.v2.permissions.PermissionsDto
import app.eluvio.wallet.util.realm.toRealmListOrEmpty

// PermissionsDto usually just has Content Permission settings, but sometimes it also hold specific
// permissions for pages and properties.

fun PermissionsDto.toContentPermissionsEntity(): PermissionsEntity {
    val dto = this
    return PermissionsEntity().apply {
        // Content permissions
        permissionItemIds = dto.permission_item_ids.toRealmListOrEmpty()
        behavior = dto.behavior?.takeIf { it.isNotEmpty() }
        alternatePageId = dto.alternate_page_id?.takeIf { it.isNotEmpty() }
    }
}

fun PermissionsDto.toPagePermissionsEntity(): PermissionsEntity {
    val dto = this
    return PermissionsEntity().apply {
        // Content permissions
        permissionItemIds = dto.page_permissions.toRealmListOrEmpty()
        behavior = dto.page_permissions_behavior?.takeIf { it.isNotEmpty() }
        alternatePageId = dto.page_permissions_alternate_page_id?.takeIf { it.isNotEmpty() }
    }
}

fun PermissionsDto.toPropertyPermissionsEntity(): PermissionsEntity {
    val dto = this
    return PermissionsEntity().apply {
        // Content permissions
        permissionItemIds = dto.property_permissions.toRealmListOrEmpty()
        behavior = dto.property_permissions_behavior?.takeIf { it.isNotEmpty() }
        alternatePageId = dto.property_permissions_alternate_page_id?.takeIf { it.isNotEmpty() }
    }
}
