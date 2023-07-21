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

class NftEntity : RealmObject {
    @PrimaryKey
    var _id: String = ""
    var contractAddress: String = ""
    var tokenId: String = ""
    var imageUrl: String = ""
    var displayName: String = ""
    var editionName: String = ""
    var description: String = ""
    var featuredMedia: RealmList<MediaEntity> = realmListOf()
    var mediaSections: RealmList<MediaSectionEntity> = realmListOf()
    var redeemableOffers: RealmList<RedeemableOfferEntity> = realmListOf()

    // Info that can be null until fetched separately from nft/info/{contractAddress}/{tokenId}
    var redeemStates: RealmList<RedeemStateEntity> = realmListOf()
    var tenant: String? = null

    override fun toString(): String {
        return "NftEntity(_id='$_id', contractAddress='$contractAddress', tokenId='$tokenId', imageUrl='$imageUrl', displayName='$displayName', editionName='$editionName', description='$description', featuredMedia=$featuredMedia, mediaSections=$mediaSections, redeemableOffers=$redeemableOffers, redeemStates=$redeemStates, tenant=$tenant)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NftEntity

        if (_id != other._id) return false
        if (contractAddress != other.contractAddress) return false
        if (tokenId != other.tokenId) return false
        if (imageUrl != other.imageUrl) return false
        if (displayName != other.displayName) return false
        if (editionName != other.editionName) return false
        if (description != other.description) return false
        if (featuredMedia.toList() != other.featuredMedia.toList()) return false
        if (mediaSections.toList() != other.mediaSections.toList()) return false
        if (redeemableOffers.toList() != other.redeemableOffers.toList()) return false
        if (redeemStates.toList() != other.redeemStates.toList()) return false
        if (tenant != other.tenant) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + contractAddress.hashCode()
        result = 31 * result + tokenId.hashCode()
        result = 31 * result + imageUrl.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + editionName.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + featuredMedia.hashCode()
        result = 31 * result + mediaSections.hashCode()
        result = 31 * result + redeemableOffers.hashCode()
        result = 31 * result + redeemStates.hashCode()
        return result
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out BaseRealmObject> = NftEntity::class
    }
}
