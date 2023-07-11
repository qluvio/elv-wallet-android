package app.eluvio.wallet.data.entities

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.EmbeddedRealmObject
import kotlin.reflect.KClass

class GalleryItemEntity : EmbeddedRealmObject {
    var name: String = ""
    var imagePath: String? = null

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out BaseRealmObject> = GalleryItemEntity::class
    }
}
