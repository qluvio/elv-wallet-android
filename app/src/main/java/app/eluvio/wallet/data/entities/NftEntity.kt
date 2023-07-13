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

    override fun toString(): String {
        return "NftEntity(_id='$_id', contractAddress='$contractAddress', tokenId='$tokenId', imageUrl='$imageUrl', displayName='$displayName', editionName='$editionName', description='$description', mediaSections=$mediaSections)"
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
        if (mediaSections.toList() != other.mediaSections.toList()) return false

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
        result = 31 * result + mediaSections.hashCode()
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
