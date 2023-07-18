package app.eluvio.wallet.network.converters

import app.eluvio.wallet.data.entities.GalleryItemEntity
import app.eluvio.wallet.data.entities.MediaCollectionEntity
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.MediaSectionEntity
import app.eluvio.wallet.data.entities.NftEntity
import app.eluvio.wallet.data.entities.RedeemableOfferEntity
import app.eluvio.wallet.network.dto.GalleryItemDto
import app.eluvio.wallet.network.dto.MediaCollectionDto
import app.eluvio.wallet.network.dto.MediaItemDto
import app.eluvio.wallet.network.dto.MediaSectionDto
import app.eluvio.wallet.network.dto.NftResponse
import app.eluvio.wallet.network.dto.RedeemableOfferDto
import app.eluvio.wallet.util.realm.toRealmInstant
import app.eluvio.wallet.util.realm.toRealmListOrEmpty
import io.realm.kotlin.ext.realmDictionaryOf
import io.realm.kotlin.ext.toRealmDictionary
import io.realm.kotlin.ext.toRealmList

fun NftResponse.toNfts(): List<NftEntity> {
    return contents.mapNotNull { dto ->
        NftEntity().apply {
            contractAddress = dto.contract_addr
            tokenId = dto.token_id
            imageUrl = dto.meta.image
            displayName = dto.meta.display_name!!
            editionName = dto.meta.edition_name ?: ""
            description = dto.meta.description ?: ""
            // Currently, additional_media_sections is required. In the future we'll probably have
            // to support additional_media for backwards compatibility.
            val additionalMediaSections =
                dto.nft_template.additional_media_sections ?: return@mapNotNull null
            featuredMedia =
                additionalMediaSections.featured_media?.map { it.toEntity() }.toRealmListOrEmpty()
            mediaSections =
                additionalMediaSections.sections?.map { it.toEntity() }.toRealmListOrEmpty()

            redeemableOffers =
                dto.nft_template.redeemable_offers?.map { it.toEntity() }
                    .toRealmListOrEmpty()
        }
    }
}

private fun RedeemableOfferDto.toEntity(): RedeemableOfferEntity {
    val dto = this
    return RedeemableOfferEntity().apply {
        name = dto.name
        offerId = dto.offer_id
        imagePath = dto.image?.path
        posterImagePath = dto.poster_image?.path
        availableAt = dto.available_at?.toRealmInstant()
        expiresAt = dto.expires_at?.toRealmInstant()
    }
}

fun MediaSectionDto.toEntity(): MediaSectionEntity {
    val dto = this
    return MediaSectionEntity().apply {
        id = dto.id
        name = dto.name
        collections = dto.collections.map { it.toEntity() }.toRealmList()
    }
}

fun MediaCollectionDto.toEntity(): MediaCollectionEntity {
    val dto = this
    return MediaCollectionEntity().apply {
        id = dto.id ?: ""
        name = dto.name ?: ""
        display = dto.display ?: ""
        media = dto.media?.map { it.toEntity() }.toRealmListOrEmpty()
    }
}

fun MediaItemDto.toEntity(): MediaEntity {
    val dto = this
    return MediaEntity().apply {
        id = dto.id
        name = dto.name
        image = dto.image ?: ""
        mediaType = dto.media_type ?: ""
        mediaFile = dto.media_file?.path ?: ""
        mediaLinks = dto.media_link?.sources
            ?.mapValues { (_, link) -> link.path }
            ?.toRealmDictionary()
            ?: realmDictionaryOf()
        tvBackgroundImage = dto.background_image_tv?.path ?: ""
        gallery = dto.gallery?.map { it.toEntity() }?.toRealmList()
    }
}

fun GalleryItemDto.toEntity(): GalleryItemEntity {
    val dto = this
    return GalleryItemEntity().apply {
        name = dto.name
        imagePath = dto.image?.path
    }
}
