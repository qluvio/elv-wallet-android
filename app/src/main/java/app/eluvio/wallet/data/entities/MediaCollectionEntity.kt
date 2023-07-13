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

class MediaCollectionEntity : RealmObject {
    @PrimaryKey
    var id: String = ""
    var name: String = ""
    var display: String = ""
    var media: RealmList<MediaEntity> = realmListOf()

    override fun toString(): String {
        return "MediaCollectionEntity(id='$id', name='$name', display='$display', media=$media)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaCollectionEntity

        if (id != other.id) return false
        if (name != other.name) return false
        if (display != other.display) return false
        if (media.toList() != other.media.toList()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + display.hashCode()
        result = 31 * result + media.hashCode()
        return result
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out BaseRealmObject> = MediaCollectionEntity::class
    }
}
