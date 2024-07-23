package app.eluvio.wallet.network.converters.v2

import app.eluvio.wallet.data.entities.v2.SearchFiltersEntity
import app.eluvio.wallet.data.entities.v2.SearchFiltersEntity.Attribute
import app.eluvio.wallet.network.dto.v2.FilterOptionsDto
import app.eluvio.wallet.network.dto.v2.SearchFilterAttributeDto
import app.eluvio.wallet.network.dto.v2.SearchFiltersDto
import app.eluvio.wallet.util.realm.toRealmListOrEmpty
import io.realm.kotlin.ext.toRealmList

fun SearchFiltersDto.toEntity(propId: String): SearchFiltersEntity {
    val dto = this
    return SearchFiltersEntity().apply {
        propertyId = propId

        tags = dto.tags.toRealmListOrEmpty()

        val attributeMap = dto.attributes.orEmpty()
            .mapValues { (_, attr) -> attr.toEntity() }
        attributes = attributeMap.values.toRealmList()

        secondaryFilter = attributeMap[dto.secondaryFilter]
        primaryFilter = attributeMap[dto.primaryFilter]
            ?.copy()
            ?.applyFilterOptions(dto.filterOptions, secondaryFilter)
    }
}

private fun Attribute.applyFilterOptions(
    filterOptions: List<FilterOptionsDto>?,
    globalSecondaryFilter: Attribute?
) = apply {
    if (filterOptions.isNullOrEmpty()) {
        // Update nextFilter to global secondary filter
        values.forEach { it.nextFilterAttribute = globalSecondaryFilter?.id }
    } else {
        // Nuke any existing tags. If they don't exist in the filter options, they're don't matter.
        values = filterOptions.map { option ->
            SearchFiltersEntity.AttributeValue().apply {
                // Server sends empty string as an "all" filter options
                value = option.primaryFilterValue.takeIf { it.isNotEmpty() }
                    ?: SearchFiltersEntity.AttributeValue.ALL
                nextFilterAttribute = option.secondaryFilterAttribute?.takeIf { it.isNotEmpty() }
                image = option.image?.path
            }
        }.toRealmListOrEmpty()
    }
}

private fun SearchFilterAttributeDto.toEntity(): Attribute {
    val dto = this
    return Attribute().apply {
        id = dto.id
        title = dto.title ?: ""
        values = dto.values
            ?.map { SearchFiltersEntity.AttributeValue.from(it) }
            .toRealmListOrEmpty()
    }
}
