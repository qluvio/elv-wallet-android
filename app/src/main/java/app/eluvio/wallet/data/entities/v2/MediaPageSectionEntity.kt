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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaPageSectionEntity

        if (id != other.id) return false
        if (items != other.items) return false
        if (title != other.title) return false
        if (subtitle != other.subtitle) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + items.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (subtitle?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "MediaPageSectionEntity(id='$id', items=$items, title=$title, subtitle=$subtitle)"
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

        companion object {
            private const val MEDIA_TYPE_LIST = "list"
            private const val MEDIA_TYPE_COLLECTION = "collection"

            // Convenience to include both type of media containers
            val MEDIA_CONTAINERS = listOf(MEDIA_TYPE_LIST, MEDIA_TYPE_COLLECTION)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SectionItemEntity

            if (media != other.media) return false
            if (expand != other.expand) return false

            return true
        }

        override fun hashCode(): Int {
            var result = media?.hashCode() ?: 0
            result = 31 * result + expand.hashCode()
            return result
        }

        override fun toString(): String {
            return "SectionItemEntity(media=$media, expand=$expand)"
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
