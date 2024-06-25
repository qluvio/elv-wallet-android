package app.eluvio.wallet.network.converters.v2

import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
import app.eluvio.wallet.network.dto.v2.MediaPageSectionDto
import app.eluvio.wallet.network.dto.v2.SectionItemDto
import app.eluvio.wallet.util.realm.toRealmListOrEmpty

private val supportedSectionTypes = setOf("media", "subproperty_link")

fun MediaPageSectionDto.toEntity(baseUrl: String): MediaPageSectionEntity {
    val dto = this

    return MediaPageSectionEntity().apply {
        id = dto.id
        items = dto.content?.mapNotNull { it.toEntity(baseUrl) }.toRealmListOrEmpty()
        title = dto.display?.title
        subtitle = dto.display?.subtitle
    }
}

private fun SectionItemDto.toEntity(baseUrl: String): MediaPageSectionEntity.SectionItemEntity? {
    val dto = this
    if (dto.type !in supportedSectionTypes) return null
    return MediaPageSectionEntity.SectionItemEntity().apply {
        mediaType = dto.mediaType
        media = dto.media?.toEntity(baseUrl)
        expand = dto.expand == true

        subpropertyId = dto.subpropertyId
        val imageLink = dto.display?.thumbnailSquare
            ?: dto.display?.thumbnailPortrait
            ?: dto.display?.thumbnailLandscape
        subpropertyImage = imageLink?.path?.let { "$baseUrl/$it" } ?: ""
    }
}
