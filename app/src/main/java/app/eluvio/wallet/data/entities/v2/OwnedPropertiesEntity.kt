package app.eluvio.wallet.data.entities.v2

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.ext.realmDictionaryOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.TypedRealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlin.reflect.KClass

interface OwnedPropertiesEntity {
    /**
     * A dictionary of properties ID->Name that the current user has access to by owning a relevant NFT.
     */
    val properties: Map<String, String>
}

class OwnedPropertiesRealmEntity : RealmObject, OwnedPropertiesEntity {
    @PrimaryKey
    var id: String = "singleton"

    override var properties = realmDictionaryOf<String>()

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out TypedRealmObject> = OwnedPropertiesRealmEntity::class
    }
}
