package app.eluvio.wallet.network.converters.v2

import app.eluvio.wallet.data.entities.GalleryItemEntity
import app.eluvio.wallet.data.entities.LiveVideoInfoEntity
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.v2.SearchFiltersEntity
import app.eluvio.wallet.network.converters.ConverterUtils
import app.eluvio.wallet.network.converters.toPathMap
import app.eluvio.wallet.network.dto.v2.GalleryItemV2Dto
import app.eluvio.wallet.network.dto.v2.MediaItemV2Dto
import app.eluvio.wallet.util.realm.toRealmDictionaryOrEmpty
import app.eluvio.wallet.util.realm.toRealmInstant
import app.eluvio.wallet.util.realm.toRealmListOrEmpty

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
        mediaType = dto.mediaType ?: dto.type
        val imageLink = dto.thumbnailSquare
            ?: dto.thumbnailPortrait
            ?: dto.thumbnailLandscape
        image = imageLink?.path?.let { "$baseUrl$it" } ?: ""
        mediaLinks = dto.mediaLink?.toPathMap().toRealmDictionaryOrEmpty()
        gallery = dto.gallery?.mapNotNull { it.toEntity() }.toRealmListOrEmpty()

        liveVideoInfo = parseLiveVideoInfo(dto)

        // Media Lists will have a list of media items under `media`, while Media Collections
        // will have a list of media lists under `mediaLists`. It is assumed that there will
        // only be one or the other, so we're just trying both with no real priority.
        mediaItemsIds = (dto.media ?: dto.mediaLists).toRealmListOrEmpty()

        attributes = dto.attributes?.map { (key, value) ->
            SearchFiltersEntity.Attribute().apply {
                id = key
                values = value.map { SearchFiltersEntity.AttributeValue.from(it) }.toRealmListOrEmpty()
            }
        }.toRealmListOrEmpty()
        tags = dto.tags.toRealmListOrEmpty()
    }
}

private fun parseLiveVideoInfo(dto: MediaItemV2Dto): LiveVideoInfoEntity? {
    if (dto.liveVideo != true) {
        return null
    }
    return LiveVideoInfoEntity().apply {
        title = dto.title
        subtitle = dto.subtitle
        headers = dto.headers.toRealmListOrEmpty()
        icons = dto.icons?.mapNotNull { it.icon?.path }.toRealmListOrEmpty()
        startTime = dto.startTime?.toRealmInstant()
        endTime = dto.endTime?.toRealmInstant()
    }
}

private fun GalleryItemV2Dto.toEntity(): GalleryItemEntity? {
    val dto = this
    return GalleryItemEntity().apply {
        // name = dto.title?
        // TODO: image should take precedence over thumbnail, if it exists
        imagePath = dto.thumbnail?.path ?: return null
        imageAspectRatio = ConverterUtils.parseAspectRatio(dto.thumbnailAspectRatio)
    }
}
