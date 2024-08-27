package app.eluvio.wallet.network.dto.v2

import app.eluvio.wallet.network.dto.v2.permissions.DtoWithPermissions
import app.eluvio.wallet.network.dto.v2.permissions.PermissionsDto
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MediaPageSectionDto(
    val content: List<SectionItemDto>?,
    @field:Json(name = "hero_items")
    val heroItems: List<HeroItemDto>?,
    val description: String?,
    val id: String,
    val type: String,
    val display: DisplaySettingsDto?,
    @field:Json(name = "primary_filter")
    val primaryFilter: String?,
    @field:Json(name = "secondary_filter")
    val secondaryFilter: String?,
    override val permissions: PermissionsDto?,
) : DtoWithPermissions

@JsonClass(generateAdapter = true)
data class SectionItemDto(
    val id: String,

    val type: String,
    @field:Json(name = "media_type")
    val mediaType: String?,
    val media: MediaItemV2Dto?,

    // TODO: handle this field
    @field:Json(name = "use_media_settings")
    val useMediaSettings: Boolean?,

    @field:Json(name = "subproperty_id")
    val subpropertyId: String?,
    // Page is also provided, but we just navigate to Main for now
    // @field:Json(name="subproperty_page_id")
    // val subpropertyPageId: String?,

    val display: DisplaySettingsDto?,
    override val permissions: PermissionsDto?,
) : DtoWithPermissions

@JsonClass(generateAdapter = true)
data class HeroItemDto(
    val display: DisplaySettingsDto?,
)
