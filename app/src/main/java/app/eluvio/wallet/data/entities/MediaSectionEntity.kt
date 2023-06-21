package app.eluvio.wallet.data.entities

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlin.reflect.KClass

class MediaSectionEntity : RealmObject {
    @PrimaryKey
    var id: String = ""
    var name: String = ""
    var collections: RealmList<MediaCollectionEntity> = realmListOf()
}

@Module
@InstallIn(SingletonComponent::class)
object MediaSectionEntityModule {
    @Provides
    @IntoSet
    fun provideEntity(): KClass<out BaseRealmObject> = MediaSectionEntity::class
}
