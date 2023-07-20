package app.eluvio.wallet.data.entities

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.ext.realmDictionaryOf
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmDictionary
import io.realm.kotlin.types.RealmInstant
import kotlin.reflect.KClass

class RedeemableOfferEntity : EmbeddedRealmObject {
    var name: String = ""
    var offerId: String = ""
    var imagePath: String? = null
    var posterImagePath: String? = null
    var availableAt: RealmInstant? = null
    var expiresAt: RealmInstant? = null

    // Relative paths to offer animations
    var animation: RealmDictionary<String> = realmDictionaryOf()

    override fun toString(): String {
        return "RedeemableOfferEntity(name='$name', offerId='$offerId', imagePath='$imagePath', posterImagePath='$posterImagePath', availableAt=$availableAt, expiresAt=$expiresAt)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RedeemableOfferEntity

        if (name != other.name) return false
        if (offerId != other.offerId) return false
        if (imagePath != other.imagePath) return false
        if (posterImagePath != other.posterImagePath) return false
        if (availableAt != other.availableAt) return false
        if (expiresAt != other.expiresAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + offerId.hashCode()
        result = 31 * result + imagePath.hashCode()
        result = 31 * result + posterImagePath.hashCode()
        result = 31 * result + (availableAt?.hashCode() ?: 0)
        result = 31 * result + (expiresAt?.hashCode() ?: 0)
        return result
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out BaseRealmObject> = RedeemableOfferEntity::class
    }
}
