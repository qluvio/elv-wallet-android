package app.eluvio.wallet.data.entities

// not cached in Realm
data class VideoOptionsEntity(
    val protocol: String,
    val uri: String,
    val drm: String,
    val licenseUri: String?
) {
    companion object {
        const val PROTOCOL_DASH = "dash"
        const val PROTOCOL_HLS = "hls"

        const val DRM_CLEAR = "clear"
        const val DRM_WIDEVINE = "widevine"
    }
}
