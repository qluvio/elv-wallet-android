package app.eluvio.wallet.network.dto.v2

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MediaPageSectionDto(
    // List of IDs of the media items in this section
    //TODO: only null for "automatic" section, which the API should later on inline with 'content'
    val content: List<SectionItemDto>?,
    val description: String?,
    val id: String,
    val type: String,
    val display: DisplaySettingsDto?,
)

@JsonClass(generateAdapter = true)
data class SectionItemDto(
    // Technically these have IDs, but we don't use them for anything.

    val type: String,
    @field:Json(name = "media_type")
    val mediaType: String?,
    val media: MediaItemV2Dto?,
    /**
     * Only applies to lists and collections.
     * If `true`, inline the list items in the section.
     */
    val expand: Boolean?,

    // TODO: handle this field
    @field:Json(name = "use_media_settings")
    val useMediaSettings: Boolean?,

    @field:Json(name = "subproperty_id")
    val subpropertyId: String?,
    // Page is also provided, but we just navigate to Main for now
    // @field:Json(name="subproperty_page_id")
    // val subpropertyPageId: String?,

    val display: DisplaySettingsDto?,
)
