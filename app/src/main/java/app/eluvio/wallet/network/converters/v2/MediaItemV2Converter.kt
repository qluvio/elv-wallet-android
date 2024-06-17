package app.eluvio.wallet.network.converters.v2

import app.eluvio.wallet.data.entities.GalleryItemEntity
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.network.converters.ConverterUtils
import app.eluvio.wallet.network.converters.toPathMap
import app.eluvio.wallet.network.dto.v2.GalleryItemV2Dto
import app.eluvio.wallet.network.dto.v2.MediaItemV2Dto
import app.eluvio.wallet.network.dto.v2.UnexpandedMediaListDto
import app.eluvio.wallet.util.realm.toRealmDictionaryOrEmpty
import app.eluvio.wallet.util.realm.toRealmListOrEmpty
import io.realm.kotlin.ext.realmListOf

fun MediaItemV2Dto.toEntity(baseUrl: String): MediaEntity {
    val dto = this

    val (imageFile, aspectRatio) = dto.mediaFile?.let { it to null }
        ?: dto.thumbnailSquare?.let { it to MediaEntity.ASPECT_RATIO_SQUARE }
        ?: dto.thumbnailPortrait?.let { it to MediaEntity.ASPECT_RATIO_POSTER }
        ?: dto.thumbnailLandscape?.let { it to MediaEntity.ASPECT_RATIO_WIDE }
        ?: (null to null)
    return MediaEntity().apply {
        id = dto.id
        name = dto.title ?: ""
        mediaFile = imageFile?.path ?: ""
        imageAspectRatio = aspectRatio
        mediaType = dto.mediaType ?: ""
        val imageLink = dto.thumbnailSquare
            ?: dto.thumbnailPortrait
            ?: dto.thumbnailLandscape
        image = imageLink?.path?.let { "$baseUrl/$it" } ?: ""
        mediaLinks = dto.mediaLink?.toPathMap().toRealmDictionaryOrEmpty()
        gallery = dto.gallery?.map { it.toEntity() }.toRealmListOrEmpty()
        // Media Lists will have a list of media items under `media`, while Media Collections
        // will have a list of media lists under `mediaLists`. It is assumed that there will
        // only be one or the other, so we are trying our luck here.
        mediaListItems =
            (dto.media?.map { it.toEntity(baseUrl) }
                ?: dto.mediaLists?.map { it.toEntity(baseUrl) })
                .toRealmListOrEmpty()
    }
}

private fun UnexpandedMediaListDto.toEntity(baseUrl: String): MediaEntity {
    val dto = this
    val (imageFile, aspectRatio) = dto.mediaFile?.let { it to null }
        ?: dto.thumbnailSquare?.let { it to MediaEntity.ASPECT_RATIO_SQUARE }
        ?: dto.thumbnailPortrait?.let { it to MediaEntity.ASPECT_RATIO_POSTER }
        ?: dto.thumbnailLandscape?.let { it to MediaEntity.ASPECT_RATIO_WIDE }
        ?: (null to null)
    return MediaEntity().apply {
        id = dto.id
        name = dto.title ?: ""
        mediaFile = imageFile?.path ?: ""
        imageAspectRatio = aspectRatio
        mediaType = dto.mediaType ?: ""
        val imageLink = dto.thumbnailSquare
            ?: dto.thumbnailPortrait
            ?: dto.thumbnailLandscape
        image = imageLink?.path?.let { "$baseUrl/$it" } ?: ""
        //TODO: we'll need to figure out how we handle some lists getting fully media objects,
        // but others only a list of string ids
        mediaListItems = realmListOf()
    }
}

private fun GalleryItemV2Dto.toEntity(): GalleryItemEntity {
    val dto = this
    return GalleryItemEntity().apply {
        // name = dto.title?
        // TODO: image should take precedence over thumbnail, if it exists
        imagePath = dto.thumbnail.path
        imageAspectRatio = ConverterUtils.parseAspectRatio(dto.thumbnailAspectRatio)
    }
}
