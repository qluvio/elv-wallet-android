package app.eluvio.wallet.network.dto.v2

import app.eluvio.wallet.network.dto.AssetLinkDto
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MediaPropertyDto(
    val description: String?,
    @field:Json(name = "header_logo")
    val headerLogo: AssetLinkDto?,
    @field:Json(name = "tv_header_logo")
    val tvHeaderLogo: AssetLinkDto?,
    val id: String,
    val image: AssetLinkDto?,
    val name: String,
    @field:Json(name = "main_page")
    val mainPage: MediaPageDto,

    val login: LoginInfoDto?
)

@JsonClass(generateAdapter = true)
data class LoginInfoDto(val settings: LoginSettingsDto?)

@JsonClass(generateAdapter = true)
data class LoginSettingsDto(val provider: String?)
