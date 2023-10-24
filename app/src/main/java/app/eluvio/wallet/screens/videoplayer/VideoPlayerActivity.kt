package app.eluvio.wallet.screens.videoplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.AudioAttributes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerView
import app.eluvio.wallet.R
import app.eluvio.wallet.data.VideoOptionsFetcher
import app.eluvio.wallet.data.stores.PlaybackStore
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
class VideoPlayerActivity : FragmentActivity(), Player.Listener {
    @Inject
    lateinit var videoOptionsFetcher: VideoOptionsFetcher

    @Inject
    lateinit var playbackStore: PlaybackStore

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
    private val mediaItemId by lazy { VideoPlayerActivityDestination.argsFrom(intent).mediaItemId }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT,  /* handleAudioFocus= */true)
            .build()
            .apply {
                addListener(this@VideoPlayerActivity)
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
            requestFocus()
        }

        //noinspection MissingInflatedId
        findViewById<DefaultTimeBar>(androidx.media3.ui.R.id.exo_progress)
            ?.setKeyTimeIncrement(5000)

        disposable = videoOptionsFetcher.fetchVideoOptions(mediaItemId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    exoPlayer?.setMediaSource(it.toMediaSource())
                    exoPlayer?.prepare()
                    exoPlayer?.seekTo(playbackStore.getPlaybackPosition(mediaItemId))
                },
                onError = {
                    Log.e("VideoPlayerFragment: Error fetching video options", it)
                    Toast.makeText(
                        this,
                        "Error loading video. Try again later.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            )
    }

    override fun onResume() {
        super.onResume()
        playerView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.currentPosition?.let { currentPosition ->
            playbackStore.setPlaybackPosition(mediaItemId, currentPosition)
        }
        playerView?.onPause()
        playerView?.player?.pause()
    }

    override fun onDestroy() {
        playerView = null
        exoPlayer?.release()
        exoPlayer = null
        super.onDestroy()
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return playerView?.dispatchKeyEvent(event) == true || super.dispatchKeyEvent(event)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
