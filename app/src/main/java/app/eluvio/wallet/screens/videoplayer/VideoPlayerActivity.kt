package app.eluvio.wallet.screens.videoplayer

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerView
import app.eluvio.wallet.R
import app.eluvio.wallet.data.VideoOptionsFetcher
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.screens.destinations.VideoPlayerActivityDestination
import app.eluvio.wallet.util.logging.Log
import com.ramcosta.composedestinations.annotation.ActivityDestination
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@MainGraph
@ActivityDestination(navArgsDelegate = VideoPlayerArgs::class)
@AndroidEntryPoint
@UnstableApi
class VideoPlayerActivity : FragmentActivity() {
    @Inject
    lateinit var videoOptionsFetcher: VideoOptionsFetcher
    private var disposable: Disposable? = null

    private var playerView: PlayerView? = null
    private var exoPlayer: ExoPlayer? = null

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            // No need to propagate event.
            // This callback should only be enabled when the controller is visible.
            playerView?.hideController()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        exoPlayer = ExoPlayer.Builder(this)
            .build()
            .apply {
                playWhenReady = true
            }
        playerView = findViewById<PlayerView>(R.id.video_player_view)?.apply {
            setShowSubtitleButton(true)
            setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            controllerHideOnTouch = false
            setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
                backPressedCallback.isEnabled = visibility == View.VISIBLE
                Log.v("Controller visibility changed: $visibility. Handling back press: ${backPressedCallback.isEnabled}")
            })
            player = exoPlayer
            showController()

            // Manually show spinner until exoplayer figures itself out
            //noinspection MissingInflatedId
            findViewById<View>(androidx.media3.ui.R.id.exo_buffering).visibility = View.VISIBLE
        }

        //noinspection MissingInflatedId
        findViewById<DefaultTimeBar>(androidx.media3.ui.R.id.exo_progress)
            ?.setKeyTimeIncrement(5000)

        val mediaItemId = VideoPlayerActivityDestination.argsFrom(intent).mediaItemId
        disposable = videoOptionsFetcher.fetchVideoOptions(mediaItemId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    exoPlayer?.setMediaSource(it.toMediaSource())
                    exoPlayer?.prepare()
                },
                onError = {
                    Log.e("VideoPlayerFragment: Error fetching video options", it)
                }
            )
    }

    override fun onResume() {
        super.onResume()
        playerView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        playerView?.onPause()
        playerView?.player?.pause()
    }

    override fun onDestroy() {
        playerView = null
        exoPlayer?.release()
        exoPlayer = null
        super.onDestroy()
    }
}
