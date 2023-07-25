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
import app.eluvio.wallet.network.dto.MediaLinkDto
import app.eluvio.wallet.network.dto.MediaSectionDto
import app.eluvio.wallet.network.dto.NftResponse
import app.eluvio.wallet.network.dto.RedeemableOfferDto
import app.eluvio.wallet.util.realm.toRealmDictionaryOrEmpty
import app.eluvio.wallet.util.realm.toRealmInstant
import app.eluvio.wallet.util.realm.toRealmListOrEmpty
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmDictionary

fun NftResponse.toNfts(): List<NftEntity> {
    val contents = contents ?: emptyList()
    val page = paging.start * 1000
    return contents.mapIndexedNotNull { index, dto ->
        // What makes this token truly unique is the combination of contract address and token id.
        // This is needed because the internal entities (sections, collections, media) have their own ID, but it's not actually unique.
        // Two MediaItems with the same ID can point to different assets, so we need to persist them differently.
        val tokenUniqueId = "${dto.contract_addr}_${dto.token_id}"
        NftEntity().apply {
            _id = tokenUniqueId
            serverIndex = page + index
            contractAddress = dto.contract_addr
            tokenId = dto.token_id
            imageUrl = dto.meta.image
            displayName = dto.meta.display_name!!
            editionName = dto.meta.edition_name ?: ""
            description = dto.meta.description ?: ""
            // Currently, additional_media_sections is required. In the future we'll probably have
            // to support additional_media for backwards compatibility.
            val additionalMediaSections =
                dto.nft_template.additional_media_sections ?: return@mapIndexedNotNull null
            featuredMedia =
                additionalMediaSections.featured_media?.map { it.toEntity(tokenUniqueId) }
                    .toRealmListOrEmpty()
            mediaSections =
                additionalMediaSections.sections?.mapNotNull { it.toEntity(tokenUniqueId) }
                    .toRealmListOrEmpty()

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
        animation = dto.animation.toPathMap()
    }
}

fun MediaSectionDto.toEntity(idPrefix: String): MediaSectionEntity? {
    val dto = this
    // Ignore sections with no collections
    dto.collections ?: return null
    return MediaSectionEntity().apply {
        id = "${idPrefix}_${dto.id}"
        name = dto.name
        collections = dto.collections.map { it.toEntity(idPrefix) }.toRealmList()
    }
}

fun MediaCollectionDto.toEntity(idPrefix: String): MediaCollectionEntity {
    val dto = this
    return MediaCollectionEntity().apply {
        id = "${idPrefix}_${dto.id}"
        name = dto.name ?: ""
        display = dto.display ?: ""
        media = dto.media?.map { it.toEntity(idPrefix) }.toRealmListOrEmpty()
    }
}

fun MediaItemDto.toEntity(idPrefix: String): MediaEntity {
    val dto = this
    return MediaEntity().apply {
        id = "${idPrefix}_${dto.id}"
        name = dto.name ?: ""
        image = dto.image ?: ""
        mediaType = dto.media_type ?: ""
        mediaFile = dto.media_file?.path ?: ""
        mediaLinks = dto.media_link.toPathMap()
        tvBackgroundImage = dto.background_image_tv?.path ?: ""
        gallery = dto.gallery?.map { it.toEntity() }.toRealmListOrEmpty()
    }
}

fun GalleryItemDto.toEntity(): GalleryItemEntity {
    val dto = this
    return GalleryItemEntity().apply {
        name = dto.name
        imagePath = dto.image?.path
    }
}

fun MediaLinkDto?.toPathMap(): RealmDictionary<String> {
    return this?.sources
        ?.mapValues { (_, link) -> link.path }
        .toRealmDictionaryOrEmpty()
}
