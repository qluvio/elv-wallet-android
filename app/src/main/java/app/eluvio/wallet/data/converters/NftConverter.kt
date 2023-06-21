package app.eluvio.wallet.data.converters

import app.eluvio.wallet.data.entities.MediaCollectionEntity
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.MediaSectionEntity
import app.eluvio.wallet.data.entities.NftEntity
import app.eluvio.wallet.network.dto.MediaCollection
import app.eluvio.wallet.network.dto.MediaItem
import app.eluvio.wallet.network.dto.MediaSection
import app.eluvio.wallet.network.dto.NftResponse
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList


fun NftResponse.toNfts(): List<NftEntity> {
    return contents.map { dto ->
        NftEntity().apply {
            contractAddress = dto.contract_addr
            tokenId = dto.token_id
            imageUrl = dto.meta.image
            displayName = dto.meta.display_name!!
            editionName = dto.meta.edition_name ?: ""
            description = dto.meta.description ?: ""
            mediaSections = dto.nft_template.additional_media_sections.sections?.toMediaSections()
                ?: realmListOf()
        }
    }
}

fun List<MediaSection>.toMediaSections(): RealmList<MediaSectionEntity> {
    return map { dto ->
        MediaSectionEntity().apply {
            id = dto.id
            name = dto.name
            collections = dto.collections.toMediaCollections()
        }
    }.toRealmList()
}

fun List<MediaCollection>.toMediaCollections(): RealmList<MediaCollectionEntity> {
    return map { dto ->
        MediaCollectionEntity().apply {
            id = dto.id ?: ""
            name = dto.name ?: ""
            display = dto.display ?: ""
            media = dto.media?.toMediaItems() ?: realmListOf()
        }
    }.toRealmList()
}

fun List<MediaItem>.toMediaItems(): RealmList<MediaEntity> {
    return map { dto ->
        MediaEntity().apply {
            name = dto.name
            image = dto.image ?: ""
            mediaType = dto.media_type ?: ""
        }
    }.toRealmList()
}
