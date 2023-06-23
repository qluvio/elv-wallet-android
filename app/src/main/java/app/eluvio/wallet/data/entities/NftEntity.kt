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

class NftEntity : RealmObject, CompositeKeyEntity {
    @PrimaryKey
    var _id: String = ""
    var contractAddress: String = ""
    var tokenId: String = ""
    var imageUrl: String = ""
    var displayName: String = ""
    var editionName: String = ""
    var description: String = ""
    var mediaSections: RealmList<MediaSectionEntity> = realmListOf()

    override fun updateKey(): String {
        _id = "${contractAddress}_${tokenId}"
        return _id
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out BaseRealmObject> = NftEntity::class
    }
}
