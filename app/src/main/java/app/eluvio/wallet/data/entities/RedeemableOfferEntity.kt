package app.eluvio.wallet.data.entities

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.ext.realmDictionaryOf
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmDictionary
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.TypedRealmObject
import io.realm.kotlin.types.annotations.Ignore
import kotlin.reflect.KClass

class RedeemableOfferEntity : EmbeddedRealmObject {
    var name: String = ""
    var description: String = ""
    var offerId: String = ""
    var imagePath: String? = null
    var posterImagePath: String? = null
    var availableAt: RealmInstant? = null
    var expiresAt: RealmInstant? = null

    // Relative paths to offer animations
    var animation: RealmDictionary<String> = realmDictionaryOf()
    var redeemAnimation: RealmDictionary<String> = realmDictionaryOf()

    @Ignore
    private val isCurrentlyAvailable: Boolean
        get() {
            val availableAt = availableAt ?: return false
            val expiresAt = expiresAt ?: return false
            return RealmInstant.now() in availableAt..expiresAt
        }

    fun getFulfillmentState(redeemState: RedeemStateEntity): FulfillmentState {
        return when {
            redeemState.status == RedeemStateEntity.RedeemStatus.REDEEMED_BY_ANOTHER_USER -> FulfillmentState.CLAIMED_BY_PREVIOUS_OWNER
            isCurrentlyAvailable -> FulfillmentState.AVAILABLE
            else -> FulfillmentState.EXPIRED
        }
    }

    enum class FulfillmentState {
        EXPIRED,
        AVAILABLE,
        CLAIMED_BY_PREVIOUS_OWNER
    }

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
        fun provideEntity(): KClass<out TypedRealmObject> = RedeemableOfferEntity::class
    }
}
