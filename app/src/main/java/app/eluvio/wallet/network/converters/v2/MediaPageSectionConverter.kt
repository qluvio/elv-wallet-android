package app.eluvio.wallet.network.converters.v2

import app.eluvio.wallet.data.AspectRatio
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
import app.eluvio.wallet.data.entities.v2.SectionItemEntity
import app.eluvio.wallet.network.dto.v2.DisplaySettingsDto
import app.eluvio.wallet.network.dto.v2.HeroItemDto
import app.eluvio.wallet.network.dto.v2.MediaPageSectionDto
import app.eluvio.wallet.network.dto.v2.SectionItemDto
import app.eluvio.wallet.util.realm.toRealmListOrEmpty

private val supportedSectionItemTypes = setOf("media", "subproperty_link", "item_purchase")

fun MediaPageSectionDto.toEntity(baseUrl: String): MediaPageSectionEntity {
    val dto = this

    return MediaPageSectionEntity().apply {
        id = dto.id
        type = dto.type
        items = (
                dto.content
                    ?.takeIf { it.isNotEmpty() }
                    ?.mapNotNull { it.toEntity(baseUrl) }
                    ?: dto.heroItems?.map { it.toEntity() }
                ).toRealmListOrEmpty()
        title = dto.display?.title
        subtitle = dto.display?.subtitle
        displayLimit = dto.display?.displayLimit?.takeIf {
            // Display limit of 0 means no limit. Which is the same handling we do for null, so just
            // turn zeros to null.
            it > 0
        }
        displayLimitType = dto.display?.displayLimitType
        displayFormat = dto.display?.displayFormat?.let { format ->
            MediaPageSectionEntity.DisplayFormat.entries
                .firstOrNull { enum -> enum.value == format }
        }
            ?: MediaPageSectionEntity.DisplayFormat.UNKNOWN

        logoPath = dto.display?.logo?.path
        logoText = dto.display?.logoText

        backgroundImagePath = dto.display?.backgroundImage?.path
        backgroundColor = dto.display?.backgroundColor?.takeIf { it.isNotEmpty() }

        primaryFilter = dto.primaryFilter
        secondaryFilter = dto.secondaryFilter
    }
}

private fun SectionItemDto.toEntity(baseUrl: String): SectionItemEntity? {
    val dto = this
    if (dto.type !in supportedSectionItemTypes) return null
    return SectionItemEntity().apply {
        id = dto.id
        mediaType = dto.mediaType
        media = dto.media?.toEntity(baseUrl)

        subpropertyId = dto.subpropertyId
        val (imageLink, aspectRatio) =
            dto.display?.thumbnailSquare?.let { it to AspectRatio.SQUARE }
                ?: dto.display?.thumbnailPortrait?.let { it to AspectRatio.POSTER }
                ?: dto.display?.thumbnailLandscape?.let { it to AspectRatio.WIDE }
                ?: (null to null)
        thumbnailUrl = imageLink?.path?.let { "$baseUrl$it" } ?: ""
        thumbnailAspectRatio = aspectRatio

        purchaseOptionsUrl =
            "http://fakeurl.com/until/api/provides/this".takeIf { dto.type == "item_purchase" }

        setDisplayFields(dto.display)
    }
}

private fun HeroItemDto.toEntity(): SectionItemEntity {
    return SectionItemEntity().apply {
        setDisplayFields(display)
    }
}

/**
 * Mutates the [MediaPageSectionEntity.SectionItemEntity] to set the display fields from the [DisplaySettingsDto].
 */
private fun SectionItemEntity.setDisplayFields(dto: DisplaySettingsDto?) {
    title = dto?.title
    subtitle = dto?.subtitle
    headers = dto?.headers.toRealmListOrEmpty()
    description = dto?.description
    logoPath = dto?.logo?.path
}
