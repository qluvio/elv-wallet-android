package app.eluvio.wallet.network.converters.v2.permissions

import app.eluvio.wallet.data.entities.v2.permissions.PermissionsEntity
import app.eluvio.wallet.network.dto.v2.permissions.PermissionsDto
import app.eluvio.wallet.util.realm.toRealmListOrEmpty

fun PermissionsDto.toEntity(): PermissionsEntity {
    val dto = this
    return PermissionsEntity().apply {
        permissionItemIds = dto.permission_item_ids.toRealmListOrEmpty()
        behavior = dto.behavior?.takeIf { it.isNotEmpty() }
        alternatePageId = dto.alternate_page_id?.takeIf { it.isNotEmpty() }
        pagePermissions = dto.page_permissions.toRealmListOrEmpty()
        pagePermissionsBehavior = dto.page_permissions_behavior?.takeIf { it.isNotEmpty() }
        pagePermissionsAlternatePageId = dto.page_permissions_alternate_page_id?.takeIf { it.isNotEmpty() }
    }
}
