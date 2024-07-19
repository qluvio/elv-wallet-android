package app.eluvio.wallet.network.dto.v2

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchRequest(
    @field:Json(name = "search_term")
    val searchTerm: String? = null,
    val tags: List<String>? = null,
    val attributes: Map<String, List<String>>? = null,
)

@JsonClass(generateAdapter = true)
data class SearchFiltersDto(
    val tags: List<String>?,
    val attributes: Map<String, SearchFilterAttributeDto>?,
    @field:Json(name = "primary_filter")
    val primaryFilter: String?,
    @field:Json(name = "secondary_filter")
    val secondaryFilter: String?,
)

@JsonClass(generateAdapter = true)
data class SearchFilterAttributeDto(
    val id: String,
    val title: String?,
    val tags: List<String>?,
)
