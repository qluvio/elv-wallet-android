package app.eluvio.wallet.network.converters.v2

import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
import app.eluvio.wallet.data.entities.v2.SectionItemEntity
import app.eluvio.wallet.data.entities.v2.display.DisplaySettingsEntity
import app.eluvio.wallet.network.converters.v2.permissions.toContentPermissionsEntity
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
        displaySettings = dto.display?.toEntity(baseUrl) ?: DisplaySettingsEntity()
        if (type == MediaPageSectionEntity.TYPE_HERO) {
            items = dto.heroItems?.map { it.toEntity(baseUrl) }.toRealmListOrEmpty()
            // Find first bg image from children and apply to self
            items
                .firstNotNullOfOrNull { it.displaySettings?.heroBackgroundImageUrl }
                ?.let { displaySettings?.heroBackgroundImageUrl = it }
        } else {
            items = dto.content?.mapNotNull { it.toEntity(baseUrl) }.toRealmListOrEmpty()
        }

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
        useMediaDisplaySettings = dto.useMediaSettings == true
        rawPermissions = dto.permissions?.toContentPermissionsEntity()

        linkData = dto.getLinkDataEntity()

        bannerImageUrl = dto.bannerImage?.toUrl(baseUrl)

        isPurchaseItem = dto.type == "item_purchase"

        displaySettings = dto.display?.toEntity(baseUrl)
    }
}

private fun HeroItemDto.toEntity(baseUrl: String): SectionItemEntity {
    val dto = this
    return SectionItemEntity().apply {
        id = dto.id
        displaySettings = dto.display?.toEntity(baseUrl)
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
