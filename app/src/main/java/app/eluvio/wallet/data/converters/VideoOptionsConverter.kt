package app.eluvio.wallet.data.converters

import app.eluvio.wallet.data.entities.VideoOptionsEntity
import app.eluvio.wallet.network.dto.PlayoutConfigDto
import app.eluvio.wallet.network.dto.VideoOptionsDto

fun VideoOptionsDto.toEntity(baseUrl: String): VideoOptionsEntity? {
    // sorted by priority:
    return dash_clear?.toEntity(baseUrl)
        ?: hls_clear?.toEntity(baseUrl)
        ?: dash_widevine?.toEntity(baseUrl)
}

private fun PlayoutConfigDto.toEntity(baseUrl: String): VideoOptionsEntity {
    return VideoOptionsEntity(
        properties.protocol,
        "${baseUrl}/${uri}",
        properties.drm ?: VideoOptionsEntity.DRM_CLEAR,
        properties.license_servers?.firstOrNull()
    )
}
