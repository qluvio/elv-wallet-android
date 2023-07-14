package app.eluvio.wallet.data.converters

import app.eluvio.wallet.data.entities.GalleryItemEntity
import app.eluvio.wallet.data.entities.MediaCollectionEntity
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.MediaSectionEntity
import app.eluvio.wallet.data.entities.NftEntity
import app.eluvio.wallet.network.dto.AdditionalMediaSectionDto
import app.eluvio.wallet.network.dto.GalleryItemDto
import app.eluvio.wallet.network.dto.MediaCollectionDto
import app.eluvio.wallet.network.dto.MediaItemDto
import app.eluvio.wallet.network.dto.MediaSectionDto
import app.eluvio.wallet.network.dto.NftResponse
import io.realm.kotlin.ext.realmDictionaryOf
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmDictionary
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList

fun NftResponse.toNfts(): List<NftEntity> {
    return contents.mapNotNull { dto ->
        // Currently, additional_media_sections is required. In the future we'll probably have
        // to support additional_media for backwards compatibility.
        dto.nft_template.additional_media_sections?.let { mediaSectionsDto ->
            NftEntity().apply {
                contractAddress = dto.contract_addr
                tokenId = dto.token_id
                imageUrl = dto.meta.image
                displayName = dto.meta.display_name!!
                editionName = dto.meta.edition_name ?: ""
                description = dto.meta.description ?: ""
                mediaSections = mediaSectionsDto.toEntity(updateKey())
            }
        }
    }
}

private fun AdditionalMediaSectionDto.toEntity(nftKey: String): RealmList<MediaSectionEntity> {
    val featuredMedia = listOfNotNull(
        featured_media?.map { it.toEntity() }
            ?.let { mediaItems ->
                MediaSectionEntity().apply {
                    id = "featured_media-$nftKey"
                    name = ""
                    collections =
                        realmListOf(MediaCollectionEntity().apply {
                            id = "featured_media-$nftKey"
                            name = ""
                            media = mediaItems.toRealmList()
                        })
                }
            })
    val sections = sections?.map { it.toEntity() } ?: emptyList()
    return (featuredMedia + sections).toRealmList()
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
        media = dto.media?.map { it.toEntity() }?.toRealmList() ?: realmListOf()
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
