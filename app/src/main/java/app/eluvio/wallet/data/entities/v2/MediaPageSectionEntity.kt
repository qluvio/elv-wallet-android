package app.eluvio.wallet.data.entities.v2

import app.eluvio.wallet.data.entities.v2.display.DisplaySettingsEntity
import app.eluvio.wallet.data.entities.v2.permissions.EntityWithPermissions
import app.eluvio.wallet.data.entities.v2.permissions.PermissionSettingsEntity
import app.eluvio.wallet.data.entities.v2.permissions.VolatilePermissionSettings
import app.eluvio.wallet.util.realm.realmEnum
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.TypedRealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlin.reflect.KClass

class MediaPageSectionEntity : RealmObject, EntityWithPermissions {

    companion object {
        const val TYPE_MANUAL = "manual"
        const val TYPE_AUTOMATIC = "automatic"
        const val TYPE_SEARCH = "search"
        const val TYPE_HERO = "hero"
    }

    @PrimaryKey
    var id: String = ""
    var type: String = ""
    var items = realmListOf<SectionItemEntity>()

    var displaySettings: DisplaySettingsEntity? = null

    var primaryFilter: String? = null
    var secondaryFilter: String? = null

    @field:Ignore
    override var resolvedPermissions: VolatilePermissionSettings? = null
    override var rawPermissions: PermissionSettingsEntity? = null
    override val permissionChildren: List<EntityWithPermissions>
        get() = items

    override val isHidden: Boolean
        get() = super.isHidden || items.all { it.isHidden }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaPageSectionEntity

        if (id != other.id) return false
        if (type != other.type) return false
        if (items != other.items) return false
        if (displaySettings != other.displaySettings) return false
        if (primaryFilter != other.primaryFilter) return false
        if (secondaryFilter != other.secondaryFilter) return false
        if (rawPermissions != other.rawPermissions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + items.hashCode()
        result = 31 * result + (displaySettings?.hashCode() ?: 0)
        result = 31 * result + (primaryFilter?.hashCode() ?: 0)
        result = 31 * result + (secondaryFilter?.hashCode() ?: 0)
        result = 31 * result + (rawPermissions?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "MediaPageSectionEntity(id='$id', type='$type', items=$items, displaySettings=$displaySettings, primaryFilter=$primaryFilter, secondaryFilter=$secondaryFilter, resolvedPermissions=$resolvedPermissions, rawPermissions=$rawPermissions)"
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out TypedRealmObject> = MediaPageSectionEntity::class
    }
}
