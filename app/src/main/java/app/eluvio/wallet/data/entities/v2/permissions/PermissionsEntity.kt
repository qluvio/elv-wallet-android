package app.eluvio.wallet.data.entities.v2.permissions

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.TypedRealmObject
import kotlin.reflect.KClass

/**
 * Permissions are used to determine if a user has access to a specific object.
 * This object can be a property, a page, a section, a sectionItem, a media item, etc.
 */
class PermissionsEntity : EmbeddedRealmObject {

    // Permission items required to access this object.
    var permissionItemIds = realmListOf<String>()

    // Content permissions trickles down to children.
    var behavior: String? = null
    var alternatePageId: String? = null

    // Only applies to Pages
    var pagePermissions = realmListOf<String>()
    var pagePermissionsBehavior: String? = null
    var pagePermissionsAlternatePageId: String? = null

    // Only applies to resolve permissions
    var authorized: Boolean? = null

    override fun toString(): String {
        return "PermissionEntity(permissionItemIds=$permissionItemIds, behavior=$behavior, alternatePageId=$alternatePageId, pagePermissions=$pagePermissions, pagePermissionsBehavior=$pagePermissionsBehavior, pagePermissionsAlternatePageId=$pagePermissionsAlternatePageId, authorized=$authorized)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PermissionsEntity

        if (permissionItemIds != other.permissionItemIds) return false
        if (behavior != other.behavior) return false
        if (alternatePageId != other.alternatePageId) return false
        if (pagePermissions != other.pagePermissions) return false
        if (pagePermissionsBehavior != other.pagePermissionsBehavior) return false
        if (pagePermissionsAlternatePageId != other.pagePermissionsAlternatePageId) return false
        if (authorized != other.authorized) return false

        return true
    }

    override fun hashCode(): Int {
        var result = permissionItemIds.hashCode()
        result = 31 * result + (behavior?.hashCode() ?: 0)
        result = 31 * result + (alternatePageId?.hashCode() ?: 0)
        result = 31 * result + pagePermissions.hashCode()
        result = 31 * result + (pagePermissionsBehavior?.hashCode() ?: 0)
        result = 31 * result + (pagePermissionsAlternatePageId?.hashCode() ?: 0)
        result = 31 * result + (authorized?.hashCode() ?: 0)
        return result
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out TypedRealmObject> = PermissionsEntity::class
    }
}
