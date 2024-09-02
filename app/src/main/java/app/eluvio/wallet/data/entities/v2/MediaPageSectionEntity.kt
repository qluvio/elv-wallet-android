package app.eluvio.wallet.data.entities.v2

import app.eluvio.wallet.data.entities.v2.permissions.EntityWithPermissions
import app.eluvio.wallet.data.entities.v2.permissions.PermissionsEntity
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

    var title: String? = null
    var subtitle: String? = null
    var displayLimit: Int? = null
    var displayLimitType: String? = null

    // Shows to the left of first item
    var logoPath: String? = null
    var logoText: String? = null

    var backgroundImagePath: String? = null

    // Hex color
    var backgroundColor: String? = null

    @Ignore
    var displayFormat: DisplayFormat by realmEnum(::displayFormatStr)
    private var displayFormatStr: String = DisplayFormat.UNKNOWN.value

    var primaryFilter: String? = null
    var secondaryFilter: String? = null

    @field:Ignore
    override var resolvedPermissions: PermissionsEntity? = null
    override var rawPermissions: PermissionsEntity? = null
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
        if (title != other.title) return false
        if (subtitle != other.subtitle) return false
        if (displayLimit != other.displayLimit) return false
        if (displayLimitType != other.displayLimitType) return false
        if (logoPath != other.logoPath) return false
        if (logoText != other.logoText) return false
        if (backgroundImagePath != other.backgroundImagePath) return false
        if (backgroundColor != other.backgroundColor) return false
        if (displayFormatStr != other.displayFormatStr) return false
        if (primaryFilter != other.primaryFilter) return false
        if (secondaryFilter != other.secondaryFilter) return false
        if (rawPermissions != other.rawPermissions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + items.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (subtitle?.hashCode() ?: 0)
        result = 31 * result + (displayLimit ?: 0)
        result = 31 * result + (displayLimitType?.hashCode() ?: 0)
        result = 31 * result + (logoPath?.hashCode() ?: 0)
        result = 31 * result + (logoText?.hashCode() ?: 0)
        result = 31 * result + (backgroundImagePath?.hashCode() ?: 0)
        result = 31 * result + (backgroundColor?.hashCode() ?: 0)
        result = 31 * result + displayFormatStr.hashCode()
        result = 31 * result + (primaryFilter?.hashCode() ?: 0)
        result = 31 * result + (secondaryFilter?.hashCode() ?: 0)
        result = 31 * result + (rawPermissions?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "MediaPageSectionEntity(id='$id', type='$type', items=$items, title=$title, subtitle=$subtitle, displayLimit=$displayLimit, displayLimitType=$displayLimitType, logoPath=$logoPath, logoText=$logoText, backgroundImagePath=$backgroundImagePath, backgroundColor=$backgroundColor, displayFormatStr='$displayFormatStr', primaryFilter=$primaryFilter, secondaryFilter=$secondaryFilter, resolvedPermissions=$resolvedPermissions, rawPermissions=$rawPermissions)"
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out TypedRealmObject> = MediaPageSectionEntity::class
    }
}
