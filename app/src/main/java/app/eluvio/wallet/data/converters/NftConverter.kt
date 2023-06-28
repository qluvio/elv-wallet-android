package app.eluvio.wallet.data.converters

import app.eluvio.wallet.data.entities.GalleryItemEntity
import app.eluvio.wallet.data.entities.MediaCollectionEntity
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.MediaSectionEntity
import app.eluvio.wallet.data.entities.NftEntity
import app.eluvio.wallet.network.dto.AdditionalMediaSectionDto
import app.eluvio.wallet.network.dto.FabricConfiguration
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

fun NftResponse.toNfts(config: FabricConfiguration): List<NftEntity> {
    return contents.map { dto ->
        NftEntity().apply {
            contractAddress = dto.contract_addr
            tokenId = dto.token_id
            imageUrl = dto.meta.image
            displayName = dto.meta.display_name!!
            editionName = dto.meta.edition_name ?: ""
            description = dto.meta.description ?: ""
            mediaSections =
                dto.nft_template.additional_media_sections.toEntity(updateKey(), config)
        }
    }
}

private fun AdditionalMediaSectionDto.toEntity(
    nftKey: String,
    config: FabricConfiguration
): RealmList<MediaSectionEntity> {
    val featuredMedia = listOfNotNull(
        featured_media?.map { it.toEntity(config) }
            ?.let { mediaItems ->
                MediaSectionEntity().apply {
                    id = "featured_media-$nftKey"
                    name = "Featured Media"
                    collections =
                        realmListOf(MediaCollectionEntity().apply {
                            id = "featured_media-$nftKey"
                            name = ""
                            media = mediaItems.toRealmList()
                        })
                }
            })
    val sections = sections?.map { it.toEntity(config) } ?: emptyList()
    return (featuredMedia + sections).toRealmList()
}

fun MediaSectionDto.toEntity(config: FabricConfiguration): MediaSectionEntity {
    val dto = this
    return MediaSectionEntity().apply {
        id = dto.id
        name = dto.name
        collections = dto.collections.map { it.toEntity(config) }.toRealmList()
    }
}

fun MediaCollectionDto.toEntity(config: FabricConfiguration): MediaCollectionEntity {
    val dto = this
    return MediaCollectionEntity().apply {
        id = dto.id ?: ""
        name = dto.name ?: ""
        display = dto.display ?: ""
        media = dto.media?.map { it.toEntity(config) }?.toRealmList() ?: realmListOf()
    }
}

fun MediaItemDto.toEntity(config: FabricConfiguration): MediaEntity {
    val dto = this
    return MediaEntity().apply {
        id = dto.id
        name = dto.name
        image = dto.image ?: ""
        mediaType = dto.media_type ?: ""
        mediaLinks = dto.media_link?.sources
            ?.mapValues { (_, link) -> link.toFullLink(config) }
            ?.toRealmDictionary()
            ?: realmDictionaryOf()
        gallery = dto.gallery?.map { it.toEntity(config) }?.toRealmList()
    }
}

fun GalleryItemDto.toEntity(config: FabricConfiguration): GalleryItemEntity {
    val dto = this
    return GalleryItemEntity().apply {
        name = dto.name
        imageUrl = dto.image?.toFullLink(config)
    }
}
