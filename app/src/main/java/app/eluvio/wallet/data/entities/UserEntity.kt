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
    val userId get() = "iusr${walletAddress.base58}"

    override fun toString(): String {
        return "UserEntity(walletAddress='$walletAddress')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserEntity

        if (walletAddress != other.walletAddress) return false

        return true
    }

    override fun hashCode(): Int {
        return walletAddress.hashCode()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out BaseRealmObject> = UserEntity::class
    }
}
