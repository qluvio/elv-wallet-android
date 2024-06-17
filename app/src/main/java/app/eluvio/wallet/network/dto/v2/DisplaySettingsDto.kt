package app.eluvio.wallet.network.dto.v2

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DisplaySettingsDto(
    val title: String?,
    val subtitle: String?,
)
