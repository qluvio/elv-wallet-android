package app.eluvio.wallet.network.converters.v2

import app.eluvio.wallet.data.entities.v2.MediaPageEntity
import app.eluvio.wallet.data.entities.v2.MediaPropertyEntity
import app.eluvio.wallet.network.dto.v2.MediaPageDto
import app.eluvio.wallet.network.dto.v2.MediaPropertyDto
import io.realm.kotlin.ext.toRealmList

fun MediaPropertyDto.toEntity(): MediaPropertyEntity {
    val dto = this
    return MediaPropertyEntity().apply {
        id = dto.id
        name = dto.name
        headerLogo = dto.tvHeaderLogo?.path ?: dto.headerLogo.path
        image = dto.image.path
        mainPage = dto.mainPage.toEntity(id)
    }
}

private fun MediaPageDto.toEntity(propertyId: String): MediaPageEntity {
    val dto = this
    val layout = dto.layout
    return MediaPageEntity().apply {
        // Page ID's aren't unique across properties (but they should be), so as a workaround we use the property ID as a prefix
        id = "$propertyId-${dto.id}"
        realId = dto.id
        sectionIds = layout.sections.toRealmList()
        backgroundImagePath = layout.backgroundImage.path
        logo = layout.logo.path
        title = layout.title
        description = layout.description
        descriptionRichText = layout.descriptionRichText
    }
}
