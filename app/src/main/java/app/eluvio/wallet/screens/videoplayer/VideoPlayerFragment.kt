package app.eluvio.wallet.screens.videoplayer

import android.os.Bundle
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.leanback.LeanbackPlayerAdapter
import app.eluvio.wallet.data.VideoOptionsFetcher
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
            isSeekEnabled = true
            host = glueHost
        }

        disposable = videoOptionsFetcher.fetchVideoOptions(mediaItemId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    player.setMediaSource(it.toMediaSource())
                    player.prepare()
                },
                onError = {
                    Log.e("VideoPlayerFragment: Error fetching video options", it)
                }
            )
    }

    override fun onPause() {
        super.onPause()
        transportControlGlue.pause()
        disposable?.dispose()
    }
}
