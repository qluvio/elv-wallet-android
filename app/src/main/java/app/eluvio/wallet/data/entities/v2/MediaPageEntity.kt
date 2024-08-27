package app.eluvio.wallet.data.entities.v2

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

class MediaPageEntity : RealmObject, EntityWithPermissions {

    /**
     * This is a composite ID we create with the containing Property's ID,
     * to make pages IDs unique across all properties.
     */
    @PrimaryKey
    var id: String = ""

    /**
     * The real ID of the page, as it comes from the API.
     * We need this for API calls that require pageId.
     */
    var realId: String = ""

    /** Not a fully qualified URL, just a relative CF path */
    var backgroundImagePath: String? = null

    /** AKA Header logo or banner */
    var logo: String? = null
    var title: String? = null
    var description: String? = ""

    /**
     * Only to be used if both [title] and [description] are missing.
     * Rich text never looks good on TV, so this is only a fallback.
     */
    var descriptionRichText: String? = null

    /**
     * Full list of Section IDs for this Media Page.
     * We might not have all of them in our local cache already, since it comes in pages from the API.
     */
    var sectionIds = realmListOf<String>()

    @Ignore
    override var resolvedPermissions: PermissionsEntity? = null
    override var rawPermissions: PermissionsEntity? = null
    override val permissionChildren: List<EntityWithPermissions>
        // Sections aren't directly connected to the object, so stop propagation here.
        get() = emptyList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaPageEntity

        if (id != other.id) return false
        if (realId != other.realId) return false
        if (backgroundImagePath != other.backgroundImagePath) return false
        if (logo != other.logo) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (descriptionRichText != other.descriptionRichText) return false
        if (sectionIds != other.sectionIds) return false
        if (resolvedPermissions != other.resolvedPermissions) return false
        if (rawPermissions != other.rawPermissions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + realId.hashCode()
        result = 31 * result + (backgroundImagePath?.hashCode() ?: 0)
        result = 31 * result + (logo?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (descriptionRichText?.hashCode() ?: 0)
        result = 31 * result + sectionIds.hashCode()
        result = 31 * result + (resolvedPermissions?.hashCode() ?: 0)
        result = 31 * result + (rawPermissions?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "MediaPageEntity(id='$id', realId='$realId', backgroundImagePath=$backgroundImagePath, logo=$logo, title=$title, description=$description, descriptionRichText=$descriptionRichText, sectionIds=$sectionIds, resolvedPermissions=$resolvedPermissions, rawPermissions=$rawPermissions)"
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out TypedRealmObject> = MediaPageEntity::class
    }
}
