package app.eluvio.wallet.data.entities.v2

import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.util.realm.RealmEnum
import app.eluvio.wallet.util.realm.realmEnum
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

class MediaPageSectionEntity : RealmObject {

    enum class DisplayFormat(override val value: String) : RealmEnum {
        UNKNOWN("unknown"),
        CAROUSEL("carousel"),
        GRID("grid");
    }

    @PrimaryKey
    var id: String = ""
    var items = realmListOf<SectionItemEntity>()

    var title: String? = null
    var subtitle: String? = null
    var displayLimit: Int? = null
    var displayLimitType: String? = null

    @Ignore
    var displayFormat: DisplayFormat by realmEnum(::displayFormatStr)
    private var displayFormatStr: String = DisplayFormat.UNKNOWN.value

    var primaryFilter: String? = null
    var secondaryFilter: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaPageSectionEntity

        if (id != other.id) return false
        if (items != other.items) return false
        if (title != other.title) return false
        if (subtitle != other.subtitle) return false
        if (displayLimit != other.displayLimit) return false
        if (displayLimitType != other.displayLimitType) return false
        if (displayFormatStr != other.displayFormatStr) return false
        if (primaryFilter != other.primaryFilter) return false
        if (secondaryFilter != other.secondaryFilter) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + items.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (subtitle?.hashCode() ?: 0)
        result = 31 * result + (displayLimit ?: 0)
        result = 31 * result + (displayLimitType?.hashCode() ?: 0)
        result = 31 * result + displayFormatStr.hashCode()
        result = 31 * result + (primaryFilter?.hashCode() ?: 0)
        result = 31 * result + (secondaryFilter?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "MediaPageSectionEntity(id='$id', items=$items, title=$title, subtitle=$subtitle, displayLimit=$displayLimit, displayLimitType=$displayLimitType, displayFormatStr='$displayFormatStr', primaryFilter=$primaryFilter, secondaryFilter=$secondaryFilter)"
    }

    class SectionItemEntity : EmbeddedRealmObject {
        // Technically these have IDs, but we don't use them for anything.
        var mediaType: String? = null

        /** Not every item is of type Media, so this field is optional. */
        var media: MediaEntity? = null

        /** This section item links to another Property. */
        var subpropertyId: String? = null
        var subpropertyImage: String? = null

        override fun toString(): String {
            return "SectionItemEntity(mediaType=$mediaType, media=$media, subpropertyId=$subpropertyId, subpropertyImage=$subpropertyImage)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SectionItemEntity

            if (mediaType != other.mediaType) return false
            if (media != other.media) return false
            if (subpropertyId != other.subpropertyId) return false
            if (subpropertyImage != other.subpropertyImage) return false

            return true
        }

        override fun hashCode(): Int {
            var result = mediaType?.hashCode() ?: 0
            result = 31 * result + (media?.hashCode() ?: 0)
            result = 31 * result + (subpropertyId?.hashCode() ?: 0)
            result = 31 * result + (subpropertyImage?.hashCode() ?: 0)
            return result
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @ElementsIntoSet
        fun provideEntity(): Set<KClass<out TypedRealmObject>> =
            setOf(MediaPageSectionEntity::class, SectionItemEntity::class)
    }
}
