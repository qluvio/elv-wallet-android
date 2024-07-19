package app.eluvio.wallet.network.dto.v2

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchFiltersDto(
    val tags: List<String>?,
    val attributes: Map<String, SearchFilterAttributeDto>?,
    val primary_filter: String?,
    val secondary_filter: String?,
)

@JsonClass(generateAdapter = true)
data class SearchFilterAttributeDto(
    val id: String,
    val title: String?,
    val tags: List<String>?,
)
