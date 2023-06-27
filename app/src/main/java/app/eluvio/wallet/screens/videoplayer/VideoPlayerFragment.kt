package app.eluvio.wallet.screens.videoplayer

import android.os.Bundle
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
import app.eluvio.wallet.data.VideoOptionsFetcher
import app.eluvio.wallet.data.entities.VideoOptionsEntity
import app.eluvio.wallet.data.stores.TokenStore
import app.eluvio.wallet.screens.destinations.VideoPlayerActivityDestination
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class VideoPlayerFragment : VideoSupportFragment() {
    @Inject
    lateinit var videoOptionsFetcher: VideoOptionsFetcher

    @Inject
    lateinit var tokenStore: TokenStore

    private lateinit var player: ExoPlayer
    private lateinit var transportControlGlue: PlaybackTransportControlGlue<LeanbackPlayerAdapter>
    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mediaItemId =
            VideoPlayerActivityDestination.argsFrom(requireActivity().intent).mediaItemId
        player = ExoPlayer.Builder(requireContext()).build().apply {
            playWhenReady = true

        }
        val glueHost = VideoSupportFragmentGlueHost(this)
        transportControlGlue = PlaybackTransportControlGlue(
            requireContext(),
            LeanbackPlayerAdapter(requireContext(), player, 50)
        ).apply {
//            isSeekEnabled = true // this break play/pause button?
            host = glueHost
        }

        disposable = videoOptionsFetcher.fetchVideoOptions(mediaItemId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    player.setMediaSource(makeMediaSource(it))
                    player.prepare()
                },
                onError = {}
            )
    }

    override fun onPause() {
        super.onPause()
        transportControlGlue.pause()
        disposable?.dispose()
    }

    private fun makeMediaSource(videoOptionsEntity: VideoOptionsEntity): MediaSource {
        val fabricToken = tokenStore.fabricToken ?: throw RuntimeException("No fabric token found")
        val tokenHeader = mapOf("Authorization" to "Bearer $fabricToken")
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(tokenHeader)
        val drmBuilder = MediaItem.DrmConfiguration.Builder(C.UUID_NIL)
            .setLicenseRequestHeaders(tokenHeader)
        when (videoOptionsEntity.drm) {
            VideoOptionsEntity.DRM_WIDEVINE -> {
                drmBuilder.setScheme(C.WIDEVINE_UUID)
                    .setLicenseUri(videoOptionsEntity.licenseUri)
                    .setMultiSession(true)
            }

            VideoOptionsEntity.DRM_CLEAR -> {
                drmBuilder.setScheme(C.CLEARKEY_UUID)
                    .setMultiSession(true)
            }

            else -> throw RuntimeException("Unsupported DRM type ${videoOptionsEntity.drm}")
        }
        val mediaItem = makeMediaItem(videoOptionsEntity.uri, drmBuilder.build())
        val mediaSourceFactory = when (videoOptionsEntity.protocol) {
            VideoOptionsEntity.PROTOCOL_DASH -> DashMediaSource.Factory(dataSourceFactory)
            VideoOptionsEntity.PROTOCOL_HLS -> HlsMediaSource.Factory(dataSourceFactory)
            else -> throw RuntimeException("Unsupported protocol ${videoOptionsEntity.protocol}")
        }
        Log.i("loading ${videoOptionsEntity.protocol}-${videoOptionsEntity.drm}")
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
}
