package app.eluvio.wallet.network.converters.v2

import app.eluvio.wallet.data.AspectRatio
import app.eluvio.wallet.data.entities.GalleryItemEntity
import app.eluvio.wallet.data.entities.LiveVideoInfoEntity
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.v2.SearchFiltersEntity
import app.eluvio.wallet.data.entities.v2.permissions.PermissionSettingsEntity
import app.eluvio.wallet.network.converters.toPathMap
import app.eluvio.wallet.network.dto.v2.GalleryItemV2Dto
import app.eluvio.wallet.network.dto.v2.MediaItemV2Dto
import app.eluvio.wallet.util.realm.toRealmDictionaryOrEmpty
import app.eluvio.wallet.util.realm.toRealmInstant
import app.eluvio.wallet.util.realm.toRealmListOrEmpty

fun MediaItemV2Dto.toEntity(baseUrl: String): MediaEntity {
    val dto = this

    val (imageFile, aspectRatio) = dto.mediaFile?.let { it to null }
        ?: dto.thumbnailSquare?.let { it to AspectRatio.SQUARE }
        ?: dto.thumbnailPortrait?.let { it to AspectRatio.POSTER }
        ?: dto.thumbnailLandscape?.let { it to AspectRatio.WIDE }
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
        playableHash = dto.mediaLink?.hashContainer?.get("source")?.toString()
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
                values =
                    value.map { SearchFiltersEntity.AttributeValue.from(it) }.toRealmListOrEmpty()
            }
        }.toRealmListOrEmpty()
        tags = dto.tags.toRealmListOrEmpty()
        rawPermissions = PermissionSettingsEntity().apply {
            permissionItemIds = dto.permissions
                ?.takeIf {
                    // Server can still send a non-empty dto.permissions list even if the item is
                    // public. In that case we should ignore the list completely.
                    dto.public != true
                }
                ?.mapNotNull { it.permission_item_id }
                ?.plus(
                    // Add a dummy permission item that will always resolve to unauthorized. This
                    // won't affect Media Items that also have permissions defined, since it's
                    // enough to own one of the permissions to gain access. But we need it to make
                    // sure "private" media items are not accessible.
                    element = ""
                )
                .toRealmListOrEmpty()
        }
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
        imageAspectRatio = AspectRatio.parse(dto.thumbnailAspectRatio)
    }
}
