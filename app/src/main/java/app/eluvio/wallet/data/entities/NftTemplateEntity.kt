package app.eluvio.wallet.data.entities

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.TypedRealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlin.reflect.KClass

class NftTemplateEntity : RealmObject {

    /** @see [NftId] */
    @PrimaryKey
    var id: String = ""
    var contractAddress: String = ""
    var imageUrl: String? = null
    var displayName: String = ""
    var editionName: String = ""
    var description: String = ""
    var descriptionRichText: String? = null
    var featuredMedia: RealmList<MediaEntity> = realmListOf()
    var mediaSections: RealmList<MediaSectionEntity> = realmListOf()

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out TypedRealmObject> = NftTemplateEntity::class
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NftTemplateEntity

        if (id != other.id) return false
        if (contractAddress != other.contractAddress) return false
        if (imageUrl != other.imageUrl) return false
        if (displayName != other.displayName) return false
        if (editionName != other.editionName) return false
        if (description != other.description) return false
        if (descriptionRichText != other.descriptionRichText) return false
        if (featuredMedia != other.featuredMedia) return false
        if (mediaSections != other.mediaSections) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + contractAddress.hashCode()
        result = 31 * result + (imageUrl?.hashCode() ?: 0)
        result = 31 * result + displayName.hashCode()
        result = 31 * result + editionName.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (descriptionRichText?.hashCode() ?: 0)
        result = 31 * result + featuredMedia.hashCode()
        result = 31 * result + mediaSections.hashCode()
        return result
    }

    override fun toString(): String {
        return "NftTemplateEntity(id='$id', contractAddress='$contractAddress', imageUrl=$imageUrl, displayName='$displayName', editionName='$editionName', description='$description', descriptionRichText=$descriptionRichText, featuredMedia=$featuredMedia, mediaSections=$mediaSections)"
    }
}
