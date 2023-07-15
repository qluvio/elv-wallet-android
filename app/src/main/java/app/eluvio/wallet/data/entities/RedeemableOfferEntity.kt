package app.eluvio.wallet.data.entities

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlin.reflect.KClass

class RedeemableOfferEntity : RealmObject, CompositeKeyEntity {
    @PrimaryKey
    var _id: String = ""
    var name: String = ""
    var contractAddress: String = ""
    var offerId: String = ""
    var imagePath: String = ""
    var posterImagePath: String = ""
    var availableAt: RealmInstant? = null
    var expiresAt: RealmInstant? = null

    override fun updateKey(): String {
        _id = "${contractAddress}_$offerId"
        return _id
    }

    override fun toString(): String {
        return "RedeemableOfferEntity(_id='$_id', name='$name', contractAddress='$contractAddress', offerId='$offerId', imagePath='$imagePath', posterImagePath='$posterImagePath', availableAt=$availableAt, expiresAt=$expiresAt)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RedeemableOfferEntity

        if (_id != other._id) return false
        if (name != other.name) return false
        if (contractAddress != other.contractAddress) return false
        if (offerId != other.offerId) return false
        if (imagePath != other.imagePath) return false
        if (posterImagePath != other.posterImagePath) return false
        if (availableAt != other.availableAt) return false
        if (expiresAt != other.expiresAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + contractAddress.hashCode()
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
