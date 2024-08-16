package app.eluvio.wallet.data.entities.v2

import app.eluvio.wallet.util.realm.realmEnum
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.TypedRealmObject
import io.realm.kotlin.types.annotations.Ignore
import kotlin.reflect.KClass

class PropertyLoginInfoRealmEntity : EmbeddedRealmObject {

    var backgroundImagePath: String? = null
    var logoPath: String? = null

    @Ignore
    var loginProvider: LoginProviders by realmEnum(::_loginProvider)
    private var _loginProvider: String = LoginProviders.UNKNOWN.value

    override fun toString(): String {
        return "PropertyLoginInfoRealmEntity(backgroundImagePath=$backgroundImagePath, logoPath=$logoPath, _loginProvider='$_loginProvider')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PropertyLoginInfoRealmEntity

        if (backgroundImagePath != other.backgroundImagePath) return false
        if (logoPath != other.logoPath) return false
        if (_loginProvider != other._loginProvider) return false

        return true
    }

    override fun hashCode(): Int {
        var result = backgroundImagePath?.hashCode() ?: 0
        result = 31 * result + (logoPath?.hashCode() ?: 0)
        result = 31 * result + _loginProvider.hashCode()
        return result
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out TypedRealmObject> = PropertyLoginInfoRealmEntity::class
    }
}
