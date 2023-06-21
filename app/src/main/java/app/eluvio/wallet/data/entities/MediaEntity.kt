package app.eluvio.wallet.data.entities

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.EmbeddedRealmObject
import kotlin.reflect.KClass

class MediaEntity : EmbeddedRealmObject {
    var name: String = ""
    var image: String = ""
    var mediaType: String = ""

    companion object {
        const val MEDIA_TYPE_IMAGE = "Image"
        const val MEDIA_TYPE_VIDEO = "Video"
    }
}

@Module
@InstallIn(SingletonComponent::class)
object MediaEntityModule {
    @Provides
    @IntoSet
    fun provideEntity(): KClass<out BaseRealmObject> = MediaEntity::class
}
