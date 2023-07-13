package app.eluvio.wallet.data.entities

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.ext.realmDictionaryOf
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.RealmDictionary
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlin.reflect.KClass

class MediaEntity : RealmObject {
    @PrimaryKey
    var id: String = ""
    var name: String = ""
    var image: String = ""
    var mediaType: String = ""

    // Relative path to file
    var mediaFile: String = ""

    // Relative paths to offerings
    var mediaLinks: RealmDictionary<String> = realmDictionaryOf()
    var gallery: RealmList<GalleryItemEntity>? = null

    override fun toString(): String {
        return "MediaEntity(id='$id', name='$name', image='$image', mediaType='$mediaType', mediaFile='$mediaFile', mediaLinks=$mediaLinks, gallery=$gallery)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaEntity

        if (id != other.id) return false
        if (name != other.name) return false
        if (image != other.image) return false
        if (mediaType != other.mediaType) return false
        if (mediaFile != other.mediaFile) return false
        if (mediaLinks != other.mediaLinks) return false
        if (gallery != other.gallery) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + mediaType.hashCode()
        result = 31 * result + mediaFile.hashCode()
        result = 31 * result + mediaLinks.hashCode()
        result = 31 * result + (gallery?.hashCode() ?: 0)
        return result
    }

    companion object {
        const val MEDIA_TYPE_AUDIO = "Audio"
        const val MEDIA_TYPE_EBOOK = "Ebook"
        const val MEDIA_TYPE_GALLERY = "Gallery"
        const val MEDIA_TYPE_HTML = "HTML"
        const val MEDIA_TYPE_IMAGE = "Image"
        const val MEDIA_TYPE_LIVE = "Live"
        const val MEDIA_TYPE_VIDEO = "Video"
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out BaseRealmObject> = MediaEntity::class
    }
}
