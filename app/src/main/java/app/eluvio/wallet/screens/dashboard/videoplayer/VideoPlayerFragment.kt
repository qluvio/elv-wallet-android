package app.eluvio.wallet.screens.dashboard.videoplayer

import android.os.Bundle
import android.widget.Toast
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManagerProvider
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.leanback.LeanbackPlayerAdapter
import app.eluvio.wallet.screens.destinations.VideoPlayerActivityDestination

@UnstableApi
class VideoPlayerFragment : VideoSupportFragment() {
    private lateinit var transportControlGlue: PlaybackTransportControlGlue<LeanbackPlayerAdapter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mediaItemId =
            VideoPlayerActivityDestination.argsFrom(requireActivity().intent).mediaItemId
        val content = buckBunnyDashWidevine // get from args in future
        Toast.makeText(requireContext(), "loading media $mediaItemId", Toast.LENGTH_SHORT).show()
        val player = ExoPlayer.Builder(requireContext()).build().apply {
            playWhenReady = true
            setMediaSource(makeMediaSource(content))
            prepare()
        }
        val glueHost = VideoSupportFragmentGlueHost(this)
        transportControlGlue = PlaybackTransportControlGlue(
            requireContext(),
            LeanbackPlayerAdapter(requireContext(), player, 50)
        ).apply {
//            isSeekEnabled = true // this break play/pause button?
            host = glueHost
        }
    }

    override fun onPause() {
        super.onPause()
        transportControlGlue.pause()
    }

    private fun makeMediaSource(content: DrmContent): MediaSource {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
        val drmConfiguration = content.drmProtocol.toDrmConfig()
        val (mediaItem, mediaSourceFactory) = when (content) {
            is DrmContent.DASH -> {
                val mediaItem = makeMediaItem(content.manifestUri, drmConfiguration)
                mediaItem to DashMediaSource.Factory(dataSourceFactory)
            }

            is DrmContent.HLS -> {
                val mediaItem = makeMediaItem(content.playlistUri, drmConfiguration)
                mediaItem to HlsMediaSource.Factory(dataSourceFactory)
            }
        }
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

    private fun DrmProtocol.toDrmConfig(): MediaItem.DrmConfiguration {
        return when (this) {
            is DrmProtocol.Widevine -> {
                MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                    .setLicenseUri(licenseUri)
                    .setMultiSession(true)
                    .build()
            }

            DrmProtocol.ClearKey -> MediaItem.DrmConfiguration.Builder(C.CLEARKEY_UUID).build()
        }
    }
}
