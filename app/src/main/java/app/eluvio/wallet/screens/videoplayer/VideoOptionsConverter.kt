package app.eluvio.wallet.screens.videoplayer

import android.annotation.SuppressLint
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManagerProvider
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import app.eluvio.wallet.data.entities.VideoOptionsEntity
import app.eluvio.wallet.util.logging.Log

@SuppressLint("UnsafeOptInUsageError")
fun VideoOptionsEntity.toMediaSource(): MediaSource {
    val dataSourceFactory = DefaultHttpDataSource.Factory()
        .setDefaultRequestProperties(tokenHeader)
    val drmBuilder = MediaItem.DrmConfiguration.Builder(C.UUID_NIL)
        .setLicenseRequestHeaders(tokenHeader)
    when (drm) {
        VideoOptionsEntity.DRM_WIDEVINE -> {
            drmBuilder.setScheme(C.WIDEVINE_UUID)
                .setLicenseUri(licenseUri)
                .setMultiSession(true)
        }

        VideoOptionsEntity.DRM_CLEAR -> {
            drmBuilder.setScheme(C.CLEARKEY_UUID)
                .setMultiSession(true)
        }

        else -> throw RuntimeException("Unsupported DRM type $drm")
    }
    val mediaItem = makeMediaItem(uri, drmBuilder.build())
    val mediaSourceFactory = when (protocol) {
        VideoOptionsEntity.PROTOCOL_DASH -> DashMediaSource.Factory(dataSourceFactory)
        VideoOptionsEntity.PROTOCOL_HLS -> HlsMediaSource.Factory(dataSourceFactory)
        else -> throw RuntimeException("Unsupported protocol $protocol")
    }
    Log.i("loading ${protocol}-${drm}")
    return mediaSourceFactory
        .setDrmSessionManagerProvider(DefaultDrmSessionManagerProvider())
        .createMediaSource(mediaItem)
}

private fun makeMediaItem(
    uri: String,
    drmConfiguration: MediaItem.DrmConfiguration
): MediaItem {
    return MediaItem.Builder()
        .setUri(uri)
        .setDrmConfiguration(drmConfiguration)
        .build()
}
