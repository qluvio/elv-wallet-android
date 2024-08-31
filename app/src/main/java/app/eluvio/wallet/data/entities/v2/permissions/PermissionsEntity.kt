package app.eluvio.wallet.data.entities.v2.permissions

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.TypedRealmObject
import io.realm.kotlin.types.annotations.Ignore
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

    // Only applies to resolve permissions
    @field:Ignore
    var authorized: Boolean? = null

    override fun toString(): String {
        return "PermissionsEntity(permissionItemIds=$permissionItemIds, behavior=$behavior, alternatePageId=$alternatePageId, authorized=$authorized)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PermissionsEntity

        if (permissionItemIds != other.permissionItemIds) return false
        if (behavior != other.behavior) return false
        if (alternatePageId != other.alternatePageId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = permissionItemIds.hashCode()
        result = 31 * result + (behavior?.hashCode() ?: 0)
        result = 31 * result + (alternatePageId?.hashCode() ?: 0)
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
