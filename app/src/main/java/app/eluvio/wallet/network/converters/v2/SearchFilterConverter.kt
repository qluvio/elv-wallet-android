package app.eluvio.wallet.network.converters.v2

import app.eluvio.wallet.data.entities.v2.SearchFiltersEntity
import app.eluvio.wallet.data.entities.v2.SearchFiltersEntity.Attribute
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

        primaryFilter = attributeMap[dto.primary_filter]
        secondaryFilter = attributeMap[dto.secondary_filter]
    }
}

private fun SearchFilterAttributeDto.toEntity(): Attribute {
    val dto = this
    return Attribute().apply {
        id = dto.id
        title = dto.title ?: ""
        tags = dto.tags.toRealmListOrEmpty()
    }
}
