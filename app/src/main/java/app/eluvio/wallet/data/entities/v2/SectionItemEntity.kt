package app.eluvio.wallet.data.entities.v2

import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.v2.permissions.EntityWithPermissions
import app.eluvio.wallet.data.entities.v2.permissions.PermissionsEntity
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

class SectionItemEntity : RealmObject, EntityWithPermissions {
    @PrimaryKey
    var id: String = ""

    var mediaType: String? = null

    /** Not every item is of type Media, so this field is optional. */
    var media: MediaEntity? = null

    /** This section item links to another Property. */
    var subpropertyId: String? = null

    var isPurchaseItem = false

    var thumbnailUrl: String? = null
    var thumbnailAspectRatio: Float? = null

    var title: String? = null
    var subtitle: String? = null
    var headers = realmListOf<String>()
    var description: String? = null
    var logoPath: String? = null

    @Ignore
    override var resolvedPermissions: PermissionsEntity? = null
    override var rawPermissions: PermissionsEntity? = null
    override val permissionChildren: List<EntityWithPermissions>
        get() = listOfNotNull(media)

    override fun toString(): String {
        return "SectionItemEntity(id='$id', mediaType=$mediaType, media=$media, subpropertyId=$subpropertyId, isPurchaseItem=$isPurchaseItem, thumbnailUrl=$thumbnailUrl, thumbnailAspectRatio=$thumbnailAspectRatio, title=$title, subtitle=$subtitle, headers=$headers, description=$description, logoPath=$logoPath, resolvedPermissions=$resolvedPermissions, rawPermissions=$rawPermissions)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SectionItemEntity

        if (id != other.id) return false
        if (mediaType != other.mediaType) return false
        if (media != other.media) return false
        if (subpropertyId != other.subpropertyId) return false
        if (isPurchaseItem != other.isPurchaseItem) return false
        if (thumbnailUrl != other.thumbnailUrl) return false
        if (thumbnailAspectRatio != other.thumbnailAspectRatio) return false
        if (title != other.title) return false
        if (subtitle != other.subtitle) return false
        if (headers != other.headers) return false
        if (description != other.description) return false
        if (logoPath != other.logoPath) return false
        if (resolvedPermissions != other.resolvedPermissions) return false
        if (rawPermissions != other.rawPermissions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (mediaType?.hashCode() ?: 0)
        result = 31 * result + (media?.hashCode() ?: 0)
        result = 31 * result + (subpropertyId?.hashCode() ?: 0)
        result = 31 * result + (isPurchaseItem?.hashCode() ?: 0)
        result = 31 * result + (thumbnailUrl?.hashCode() ?: 0)
        result = 31 * result + (thumbnailAspectRatio?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (subtitle?.hashCode() ?: 0)
        result = 31 * result + headers.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (logoPath?.hashCode() ?: 0)
        result = 31 * result + (resolvedPermissions?.hashCode() ?: 0)
        result = 31 * result + (rawPermissions?.hashCode() ?: 0)
        return result
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out TypedRealmObject> = SectionItemEntity::class
    }
}
