package app.eluvio.wallet.data.entities.v2

import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.v2.permissions.EntityWithPermissions
import app.eluvio.wallet.data.entities.v2.permissions.PermissionsEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.EmbeddedRealmObject
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

    /** When exists, this item is a link to another page/property */
    var linkData: LinkData? = null

    var isPurchaseItem = false

    var thumbnailUrl: String? = null
    var thumbnailAspectRatio: Float? = null

    // This field is defined only if the SectionItem is inside a Banner section.
    var bannerImageUrl: String? = null

    var title: String? = null
    var subtitle: String? = null
    var headers = realmListOf<String>()
    var description: String? = null
    var logoPath: String? = null

    @field:Ignore
    override var resolvedPermissions: PermissionsEntity? = null
    override var rawPermissions: PermissionsEntity? = null
    override val permissionChildren: List<EntityWithPermissions>
        get() = listOfNotNull(media)

    override fun toString(): String {
        return "SectionItemEntity(id='$id', mediaType=$mediaType, media=$media, linkData=$linkData, isPurchaseItem=$isPurchaseItem, thumbnailUrl=$thumbnailUrl, thumbnailAspectRatio=$thumbnailAspectRatio, bannerImageUrl=$bannerImageUrl, title=$title, subtitle=$subtitle, headers=$headers, description=$description, logoPath=$logoPath, resolvedPermissions=$resolvedPermissions, rawPermissions=$rawPermissions)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SectionItemEntity

        if (id != other.id) return false
        if (mediaType != other.mediaType) return false
        if (media != other.media) return false
        if (linkData != other.linkData) return false
        if (isPurchaseItem != other.isPurchaseItem) return false
        if (thumbnailUrl != other.thumbnailUrl) return false
        if (thumbnailAspectRatio != other.thumbnailAspectRatio) return false
        if (bannerImageUrl != other.bannerImageUrl) return false
        if (title != other.title) return false
        if (subtitle != other.subtitle) return false
        if (headers != other.headers) return false
        if (description != other.description) return false
        if (logoPath != other.logoPath) return false
        if (rawPermissions != other.rawPermissions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (mediaType?.hashCode() ?: 0)
        result = 31 * result + (media?.hashCode() ?: 0)
        result = 31 * result + (linkData?.hashCode() ?: 0)
        result = 31 * result + isPurchaseItem.hashCode()
        result = 31 * result + (thumbnailUrl?.hashCode() ?: 0)
        result = 31 * result + (thumbnailAspectRatio?.hashCode() ?: 0)
        result = 31 * result + (bannerImageUrl?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (subtitle?.hashCode() ?: 0)
        result = 31 * result + headers.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (logoPath?.hashCode() ?: 0)
        result = 31 * result + (rawPermissions?.hashCode() ?: 0)
        return result
    }

    class LinkData : EmbeddedRealmObject {
        /** This section item links to a property/page. */
        var linkPropertyId: String? = null
        var linkPageId: String? = null
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @ElementsIntoSet
        fun provideEntity(): Set<KClass<out TypedRealmObject>> =
            setOf(SectionItemEntity::class, LinkData::class)
    }
}
