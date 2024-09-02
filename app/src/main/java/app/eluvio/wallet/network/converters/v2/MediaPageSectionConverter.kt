package app.eluvio.wallet.network.converters.v2

import app.eluvio.wallet.data.AspectRatio
import app.eluvio.wallet.data.entities.v2.DisplayFormat
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
import app.eluvio.wallet.data.entities.v2.SectionItemEntity
import app.eluvio.wallet.network.converters.v2.permissions.toContentPermissionsEntity
import app.eluvio.wallet.network.dto.v2.DisplaySettingsDto
import app.eluvio.wallet.network.dto.v2.HeroItemDto
import app.eluvio.wallet.network.dto.v2.MediaPageSectionDto
import app.eluvio.wallet.network.dto.v2.SectionItemDto
import app.eluvio.wallet.util.realm.toRealmListOrEmpty

private const val TYPE_PROPERTY_LINK = "property_link"
private const val TYPE_SUBPROPERTY_LINK = "subproperty_link"
private const val TYPE_PAGE_LINK = "page_link"

private val supportedSectionItemTypes =
    setOf("media", "item_purchase", TYPE_PROPERTY_LINK, TYPE_SUBPROPERTY_LINK, TYPE_PAGE_LINK)

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
            DisplayFormat.from(format)
        } ?: DisplayFormat.UNKNOWN

        logoPath = dto.display?.logo?.path
        logoText = dto.display?.logoText

        backgroundImagePath = dto.display?.backgroundImage?.path
        backgroundColor = dto.display?.backgroundColor?.takeIf { it.isNotEmpty() }

        primaryFilter = dto.primaryFilter
        secondaryFilter = dto.secondaryFilter

        rawPermissions = dto.permissions?.toContentPermissionsEntity()
    }
}

private fun SectionItemDto.toEntity(baseUrl: String): SectionItemEntity? {
    val dto = this
    if (dto.type !in supportedSectionItemTypes) return null
    return SectionItemEntity().apply {
        id = dto.id
        mediaType = dto.mediaType
        media = dto.media?.toEntity(baseUrl)
        rawPermissions = dto.permissions?.toContentPermissionsEntity()

        linkData = dto.getLinkDataEntity()
        val (imageLink, aspectRatio) =
            dto.display?.thumbnailSquare?.let { it to AspectRatio.SQUARE }
                ?: dto.display?.thumbnailPortrait?.let { it to AspectRatio.POSTER }
                ?: dto.display?.thumbnailLandscape?.let { it to AspectRatio.WIDE }
                ?: (null to null)
        thumbnailUrl = imageLink?.path?.let { "$baseUrl$it" } ?: ""
        thumbnailAspectRatio = aspectRatio

        bannerImageUrl = dto.bannerImage?.path?.let { "$baseUrl$it" }

        isPurchaseItem = dto.type == "item_purchase"

        setDisplayFields(dto.display)
    }
}

private fun HeroItemDto.toEntity(): SectionItemEntity {
    val dto = this
    return SectionItemEntity().apply {
        id = dto.id
        setDisplayFields(display)
    }
}

/**
 * Returns a [SectionItemEntity.LinkData] object if this SectionItem represents a link.
 * Because the server doesn't clear irrelevant fields, we can't just find the first non-null link
 * field. We have to only look at the fields for the corresponding type.
 */
private fun SectionItemDto.getLinkDataEntity(): SectionItemEntity.LinkData? {
    return when (type) {
        TYPE_PROPERTY_LINK -> {
            SectionItemEntity.LinkData().apply {
                linkPropertyId = propertyId
                linkPageId = propertyPageId
            }
        }

        TYPE_SUBPROPERTY_LINK -> {
            SectionItemEntity.LinkData().apply {
                linkPropertyId = subpropertyId
                linkPageId = subpropertyPageId
            }
        }

        TYPE_PAGE_LINK -> {
            SectionItemEntity.LinkData().apply {
                linkPageId = pageId
            }
        }

        else -> null
    }
}

/**
 * Mutates the [SectionItemEntity] to set the display fields from the [DisplaySettingsDto].
 */
private fun SectionItemEntity.setDisplayFields(dto: DisplaySettingsDto?) {
    title = dto?.title
    subtitle = dto?.subtitle
    headers = dto?.headers.toRealmListOrEmpty()
    description = dto?.description
    logoPath = dto?.logo?.path
}
