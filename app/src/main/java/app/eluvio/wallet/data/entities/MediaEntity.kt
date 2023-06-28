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
    var mediaLinks: RealmDictionary<String> = realmDictionaryOf()
    var gallery: RealmList<GalleryItemEntity>? = null

    companion object {
        const val MEDIA_TYPE_IMAGE = "Image"
        const val MEDIA_TYPE_VIDEO = "Video"
        const val MEDIA_TYPE_EBOOK = "Ebook"
        const val MEDIA_TYPE_GALLERY = "Gallery"
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out BaseRealmObject> = MediaEntity::class
    }
}
