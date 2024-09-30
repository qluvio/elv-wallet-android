package app.eluvio.wallet.network.converters.v2

import app.eluvio.wallet.data.entities.v2.PropertySearchFiltersEntity
import app.eluvio.wallet.data.entities.v2.SearchFilterAttribute
import app.eluvio.wallet.network.dto.v2.FilterOptionsDto
import app.eluvio.wallet.network.dto.v2.GetFiltersResponse
import app.eluvio.wallet.network.dto.v2.SearchFilterAttributeDto
import app.eluvio.wallet.network.dto.v2.SearchFiltersDto
import app.eluvio.wallet.util.realm.toRealmListOrEmpty
import io.realm.kotlin.ext.toRealmDictionary

fun GetFiltersResponse.toEntity(propId: String, baseUrl: String): PropertySearchFiltersEntity {
    val dto = this
    return PropertySearchFiltersEntity().apply {
        propertyId = propId

        tags = dto.tags.toRealmListOrEmpty()

        val attributeMap = dto.attributes.orEmpty()
            .mapValues { (_, attr) -> attr.toEntity() }
        attributes = attributeMap.toRealmDictionary()

        primaryFilter = primarySearchAttributeEntity(
            dto.primaryFilter,
            dto.secondaryFilter,
            dto.filterOptions,
            attributeMap,
            baseUrl
        )
    }
}

fun SearchFiltersDto.toEntity(tagsAndAttributes: PropertySearchFiltersEntity, baseUrl: String): SearchFilterAttribute? {
    return primarySearchAttributeEntity(
        primaryFilter,
        secondaryFilter,
        filterOptions,
        tagsAndAttributes.attributes,
        baseUrl
    )
}

private fun primarySearchAttributeEntity(
    primaryFilter: String?,
    secondaryFilter: String?,
    filterOptions: List<FilterOptionsDto>?,
    attributeMap: Map<String, SearchFilterAttribute?>,
    baseUrl: String
): SearchFilterAttribute? {
    val secondaryFilterFallback = attributeMap[secondaryFilter]
    return attributeMap[primaryFilter]
        ?.copy()
        ?.applyFilterOptions(filterOptions, secondaryFilterFallback, baseUrl)
}

private fun SearchFilterAttribute.applyFilterOptions(
    filterOptions: List<FilterOptionsDto>?,
    secondaryFilterFallback: SearchFilterAttribute?,
    baseUrl: String
) = apply {
    if (filterOptions.isNullOrEmpty()) {
        // Update nextFilter to global secondary filter
        values.forEach { it.nextFilterAttribute = secondaryFilterFallback?.id }
    } else {
        // Nuke any existing tags. If they don't exist in the filter options, they're don't matter.
        values = filterOptions.map { option ->
            SearchFilterAttribute.Value().apply {
                // Server sends empty string as an "all" filter options
                value = option.primaryFilterValue.takeIf { it.isNotEmpty() }
                    ?: SearchFilterAttribute.Value.ALL
                nextFilterAttribute = option.secondaryFilterAttribute?.takeIf { it.isNotEmpty() }
                imageUrl = option.image?.toUrl(baseUrl)
            }
        }.toRealmListOrEmpty()
    }
}

private fun SearchFilterAttributeDto.toEntity(): SearchFilterAttribute {
    val dto = this
    return SearchFilterAttribute().apply {
        id = dto.id
        title = dto.title ?: ""
        values = dto.values
            ?.map { SearchFilterAttribute.Value.from(it) }
            .toRealmListOrEmpty()
    }
}
