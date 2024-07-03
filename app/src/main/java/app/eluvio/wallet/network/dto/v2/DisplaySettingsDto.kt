package app.eluvio.wallet.network.dto.v2

import app.eluvio.wallet.network.dto.AssetLinkDto
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DisplaySettingsDto(
    val title: String?,
    val subtitle: String?,
    @field:Json(name = "thumbnail_image_landscape")
    val thumbnailLandscape: AssetLinkDto?,
    @field:Json(name = "thumbnail_image_portrait")
    val thumbnailPortrait: AssetLinkDto?,
    @field:Json(name = "thumbnail_image_square")
    val thumbnailSquare: AssetLinkDto?,
    @field:Json(name = "display_limit")
    val displayLimit: Int?
)
