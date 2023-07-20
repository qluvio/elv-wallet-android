package app.eluvio.wallet.network.converters

import app.eluvio.wallet.data.entities.VideoOptionsEntity
import app.eluvio.wallet.network.dto.PlayoutConfigDto
import app.eluvio.wallet.network.dto.VideoOptionsDto

fun VideoOptionsDto.toEntity(baseUrl: String, fabricToken: String?): VideoOptionsEntity? {
    // sorted by priority:
    return dash_clear?.toEntity(baseUrl, fabricToken)
        ?: hls_clear?.toEntity(baseUrl, fabricToken)
        ?: dash_widevine?.toEntity(baseUrl, fabricToken)
}

private fun PlayoutConfigDto.toEntity(baseUrl: String, fabricToken: String?): VideoOptionsEntity {
    val tokenHeader = if (fabricToken == null) {
        emptyMap()
    } else {
        mapOf("Authorization" to "Bearer $fabricToken")
    }
    return VideoOptionsEntity(
        protocol = properties.protocol,
        uri = "${baseUrl}/${uri}",
        drm = properties.drm ?: VideoOptionsEntity.DRM_CLEAR,
        licenseUri = properties.license_servers?.firstOrNull(),
        tokenHeader = tokenHeader
    )
}
