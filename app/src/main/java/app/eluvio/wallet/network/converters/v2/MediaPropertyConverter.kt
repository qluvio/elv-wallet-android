package app.eluvio.wallet.network.converters.v2

import app.eluvio.wallet.data.entities.v2.LoginProviders
import app.eluvio.wallet.data.entities.v2.MediaPageEntity
import app.eluvio.wallet.data.entities.v2.MediaPropertyEntity
import app.eluvio.wallet.data.entities.v2.PropertyLoginInfoRealmEntity
import app.eluvio.wallet.network.converters.v2.permissions.toContentPermissionsEntity
import app.eluvio.wallet.network.converters.v2.permissions.toPagePermissionsEntity
import app.eluvio.wallet.network.converters.v2.permissions.toPermissionStateEntities
import app.eluvio.wallet.network.converters.v2.permissions.toPropertyPermissionsEntity
import app.eluvio.wallet.network.dto.v2.LoginInfoDto
import app.eluvio.wallet.network.dto.v2.MediaPageDto
import app.eluvio.wallet.network.dto.v2.MediaPropertyDto
import io.realm.kotlin.ext.toRealmList

fun MediaPropertyDto.toEntity(): MediaPropertyEntity? {
    val dto = this
    return MediaPropertyEntity().apply {
        id = dto.id
        name = dto.name
        headerLogo = dto.tvHeaderLogo?.path ?: dto.headerLogo?.path ?: ""
        // We can't handle properties without images
        image = dto.image?.path?.takeIf { it.isNotEmpty() } ?: return null
        mainPage = dto.mainPage.toEntity(id)
        loginInfo = dto.login?.toEntity()

        permissionStates = dto.toPermissionStateEntities()
        rawPermissions = dto.permissions?.toContentPermissionsEntity()
        propertyPermissions = dto.permissions?.toPropertyPermissionsEntity()
    }
}

private fun LoginInfoDto.toEntity(): PropertyLoginInfoRealmEntity {
    val dto = this
    return PropertyLoginInfoRealmEntity().apply {
        backgroundImagePath =
            dto.styling?.backgroundImageTv?.path ?: dto.styling?.backgroundImageDesktop?.path
        logoPath = dto.styling?.logoTv?.path ?: dto.styling?.logo?.path
        loginProvider = LoginProviders.from(dto.settings?.provider)
    }
}

fun MediaPageDto.toEntity(propertyId: String): MediaPageEntity {
    val dto = this
    val layout = dto.layout
    return MediaPageEntity().apply {
        // Page ID's aren't unique across properties (but they should be), so as a workaround we use the property ID as a prefix
        uid = MediaPageEntity.uid(propertyId, dto.id)
        id = dto.id
        sectionIds = layout.sections.toRealmList()
        backgroundImagePath = layout.backgroundImage?.path
        logo = layout.logo?.path
        title = layout.title
        description = layout.description
        descriptionRichText = layout.descriptionRichText
        rawPermissions = dto.permissions?.toContentPermissionsEntity()
        pagePermissions = dto.permissions?.toPagePermissionsEntity()
    }
}
