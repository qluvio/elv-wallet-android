package app.eluvio.wallet.data

import app.eluvio.wallet.data.entities.v2.MediaPageEntity
import app.eluvio.wallet.data.entities.v2.MediaPropertyEntity
import app.eluvio.wallet.data.entities.v2.permissions.EntityWithPermissions
import app.eluvio.wallet.data.entities.v2.permissions.PermissionStatesEntity
import app.eluvio.wallet.data.entities.v2.permissions.PermissionsEntity

object PermissionResolver {
    /**
     * Recursively resolves permissions and updates entities with resolved permissions.
     */
    fun resolvePermissions(
        entity: EntityWithPermissions,
        parentPermissions: PermissionsEntity?,
        permissionStates: Map<String, PermissionStatesEntity?>
    ) {
        // Special cases
        resolveSpecialPermissions(entity, permissionStates)

        resolveContentPermissions(entity, parentPermissions, permissionStates)

        // Iterate children and update their permissions too
        entity.permissionChildren.forEach { child ->
            resolvePermissions(child, entity.resolvedPermissions, permissionStates)
        }
    }

    /**
     * Updates [entity] with resolved permissions.
     */
    private fun resolveContentPermissions(
        entity: EntityWithPermissions,
        parentPermissions: PermissionsEntity?,
        permissionStates: Map<String, PermissionStatesEntity?>
    ) {
        entity.resolvedPermissions =
            if (parentPermissions == null) {
                // top level, resolve with [entity] as parent.
                entity.rawPermissions?.let {
                    merge(
                        parent = it,
                        child = null,
                        permissionStates = permissionStates
                    )
                }
            } else {
                merge(
                    parent = parentPermissions,
                    child = entity.rawPermissions,
                    permissionStates = permissionStates
                )
            }
    }

    private fun resolveSpecialPermissions(
        entity: EntityWithPermissions,
        permissionStates: Map<String, PermissionStatesEntity?>
    ) {
        if (entity is MediaPropertyEntity) {
            entity.propertyPermissions?.let {
                entity.propertyPermissions = merge(
                    parent = it,
                    child = null,
                    permissionStates = permissionStates
                )
            }
        } else if (entity is MediaPageEntity) {
            entity.pagePermissions?.let {
                entity.pagePermissions = merge(
                    parent = it,
                    child = null,
                    permissionStates = permissionStates
                )
            }
        }
    }

    /**
     * Returns a new [PermissionsEntity] with merged permissions.
     * Note that [parent] and [child] are not treated equally, and parent permissions take over
     * once we hit an unauthorized state.
     */
    private fun merge(
        parent: PermissionsEntity,
        child: PermissionsEntity?,
        permissionStates: Map<String, PermissionStatesEntity?>
    ): PermissionsEntity {
        val result = PermissionsEntity()
        val primary: PermissionsEntity
        val fallback: PermissionsEntity
        if (child == null) {
            // Child has nothing defined, [authorized] will be the same as the parent's,
            // unless it still needs to be calculated.
            result.authorized = parent.authorized ?: with(parent.permissionItemIds) {
                isEmpty() || any { permissionStates[it]?.authorized == true }
            }
            primary = parent
            fallback = parent
        } else if (parent.authorized == false) {
            // Parent is not authorized, everything down the line is not authorized
            // and will inherit behavior, unless it's not already set.
            result.authorized = false
            primary = parent
            fallback = child
        } else {
            result.authorized = with(child.permissionItemIds) {
                isEmpty() || any { permissionStates[it]?.authorized == true }
            }
            // Parent is authorized, child will have to check its own permissions.
            primary = child
            fallback = parent
        }
        // Copy content permissions / behavior
        result.behavior = primary.behavior ?: fallback.behavior
        result.alternatePageId = primary.alternatePageId ?: fallback.alternatePageId
        result.permissionItemIds =
            primary.permissionItemIds.takeIf { it.isNotEmpty() } ?: fallback.permissionItemIds
        return result
    }
}
