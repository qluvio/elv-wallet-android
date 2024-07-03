package app.eluvio.wallet.data.entities.v2

import app.eluvio.wallet.data.entities.MediaEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.TypedRealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlin.reflect.KClass

class MediaPageSectionEntity : RealmObject {
    @PrimaryKey
    var id: String = ""
    var items = realmListOf<SectionItemEntity>()

    var title: String? = null
    var subtitle: String? = null
    var displayLimit: Int? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaPageSectionEntity

        if (id != other.id) return false
        if (items != other.items) return false
        if (title != other.title) return false
        if (subtitle != other.subtitle) return false
        if (displayLimit != other.displayLimit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + items.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (subtitle?.hashCode() ?: 0)
        result = 31 * result + (displayLimit ?: 0)
        return result
    }

    override fun toString(): String {
        return "MediaPageSectionEntity(id='$id', items=$items, title=$title, subtitle=$subtitle, displayLimit=$displayLimit)"
    }

    class SectionItemEntity : EmbeddedRealmObject {
        // Technically these have IDs, but we don't use them for anything.
        var mediaType: String? = null

        /** Not every item is of type Media, so this field is optional. */
        var media: MediaEntity? = null

        /**
         *  Only applies to Lists and Collections.
         *  When `false`, the list itself will appear in the section, linking to a list details page.
         *  When `true`, the items in the list will be inlined into the containing section.
         */
        var expand: Boolean = false

        /** This section item links to another Property. */
        var subpropertyId: String? = null
        var subpropertyImage: String? = null

        companion object {
            private const val MEDIA_TYPE_LIST = "list"
            private const val MEDIA_TYPE_COLLECTION = "collection"

            // Convenience to include both type of media containers
            val MEDIA_CONTAINERS = listOf(MEDIA_TYPE_LIST, MEDIA_TYPE_COLLECTION)
        }

        override fun toString(): String {
            return "SectionItemEntity(mediaType=$mediaType, media=$media, expand=$expand, subpropertyId=$subpropertyId, subpropertyImage=$subpropertyImage)"
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
