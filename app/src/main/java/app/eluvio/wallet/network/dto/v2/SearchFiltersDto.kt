package app.eluvio.wallet.network.dto.v2

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchFiltersDto(
    val tags: List<String>,
    val attributes: Map<String, String>,
)