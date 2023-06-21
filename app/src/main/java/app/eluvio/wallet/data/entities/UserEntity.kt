package app.eluvio.wallet.data.entities

import app.eluvio.wallet.util.base58
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlin.reflect.KClass

class UserEntity : RealmObject {
    @PrimaryKey
    var walletAddress: String = ""

    @Ignore
    val userId = "iusr${walletAddress.base58}"

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out BaseRealmObject> = UserEntity::class
    }
}
