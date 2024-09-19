package app.eluvio.wallet.screens.common

import android.annotation.SuppressLint
import android.content.Context
import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView
import app.eluvio.wallet.util.logging.Log

@UnstableApi
// just a wrapper to log some calls
class MyMediaSource(private val delegate: MediaSource) : MediaSource by delegate {
    override fun releaseSource(caller: MediaSource.MediaSourceCaller) {
        Log.w("stav: media source released ${delegate.mediaItem.mediaId}")
        delegate.releaseSource(caller)
    }

    override fun disable(caller: MediaSource.MediaSourceCaller) {
        Log.w("stav: media source disabled ${delegate.mediaItem.mediaId}")
        delegate.disable(caller)
    }

    override fun releasePeriod(mediaPeriod: MediaPeriod) {
        Log.w("stav: period release ${delegate.mediaItem.mediaId}")
        delegate.releasePeriod(mediaPeriod)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MyMediaSource

        return delegate == other.delegate
    }

    override fun hashCode(): Int {
        return delegate.hashCode()
    }

}

@SuppressLint("UnsafeOptInUsageError")
class PreloadingExoPlayer(context: Context) {
    //    private var currentPlayingIndex = C.INDEX_UNSET
//    private val preloadManager: DefaultPreloadManager
    private val exoPlayer: ExoPlayer

    init {
//        val preloadControl = object : TargetPreloadStatusControl<Int> {
//            override fun getTargetPreloadStatus(rankingData: Int): DefaultPreloadManager.Status? {
//                if (abs(rankingData - currentPlayingIndex) == 2) {
//                    return DefaultPreloadManager.Status(STAGE_LOADED_TO_POSITION_MS, 500L)
//                } else if (abs(rankingData - currentPlayingIndex) == 1) {
//                    return DefaultPreloadManager.Status(STAGE_LOADED_TO_POSITION_MS, 1000L)
//                }
//                return null
//            }
//        }

//        val bandwidthMeter = DefaultBandwidthMeter.getSingletonInstance(context)
//
//        val trackSelector = DefaultTrackSelector(context)
//        trackSelector.init({}, bandwidthMeter)

//        val loadControl = DefaultLoadControl.Builder()
//            .setPrioritizeTimeOverSizeThresholds(true)
//            .setBufferDurationsMs(
//                // Alexey said so. https://www.reddit.com/r/RedditEng/comments/1af2d8d/improving_video_playback_with_exoplayer/
//                20_000,
//                20_000,
//                1_000,
//                1_000,
//            )
//            .build()
//
//        val renderersFactory = DefaultRenderersFactory(context)
//        preloadManager = DefaultPreloadManager(
//            preloadControl,
//            DefaultMediaSourceFactory(context),
//            trackSelector,
//            bandwidthMeter,
//            DefaultRendererCapabilitiesList.Factory(renderersFactory),
//            loadControl.allocator,
//            videoPlayerLooper,
//        )
        exoPlayer = ExoPlayer.Builder(context)
            .setPlaybackLooper(videoPlayerLooper)
//            .setTrackSelector(trackSelector)
//            .setLooper(Looper.getMainLooper())
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0f // temp
                playWhenReady = false
            }
    }

    private var lastView: PlayerView? = null
    private var lastListener: Player.Listener? = null

    fun maybeSetAndPlay(view: PlayerView, listener: Player.Listener, media: MediaSource) {
        if (lastView != view) {
            lastView?.player = null
            lastView = view
            view.player = exoPlayer
        }

        if (lastListener != listener) {
            lastListener?.let { exoPlayer.removeListener(it) }
            lastListener = listener
            exoPlayer.addListener(listener)
        }

        Log.w("stav: same item =${exoPlayer.currentMediaItem == media.mediaItem} isplaying=${exoPlayer.isPlaying}")
        if (exoPlayer.currentMediaItem != media.mediaItem || !exoPlayer.isPlaying) {
            play(media)
        }
    }


    private val items = mutableListOf<MediaSource>()

    fun prepare(mediaSource: MediaSource) {
        items.add(mediaSource)
//        preloadManager.add(mediaSource, items.size - 1)
//        val preloadMediaSource = preloadManager.getMediaSource(mediaSource.mediaItem)
//        exoPlayer.addMediaSource(preloadMediaSource!!)

        exoPlayer.addMediaSource(mediaSource)

        if (exoPlayer.playbackState == Player.STATE_IDLE) {
            println("stav: preparing exo")
            exoPlayer.prepare()
        }
    }

    fun forget(mediaSource: MediaSource) {
        val index = items.indexOf(mediaSource)
        items.remove(mediaSource)
//        preloadManager.remove(mediaSource)
        exoPlayer.removeMediaItem(index)
    }

    fun play(mediaSource: MediaSource) {
        val index = items.indexOf(mediaSource)
        if (index == -1) {
            Log.e("stav: mediaSource not found in items")
            return
        }
//        preloadManager.setCurrentPlayingIndex(index)
//        preloadManager.getMediaSource(mediaSource.mediaItem)?.let {
        Log.e("stav: telling exo to play index $index")
        exoPlayer.seekTo(index, C.TIME_UNSET)
//            exoPlayer.setMediaSource(it)
        exoPlayer.playWhenReady = true
//            exoPlayer.prepare()
        //}
    }


}

/**
 * Returns a dummy MutableState that does not cause render when setting it
 */
@Composable
fun <T> rememberRef(): MutableState<T?> {
    // for some reason it always recreated the value with vararg keys,
    // leaving out the keys as a parameter for remember for now
    return remember {
        object : MutableState<T?> {
            override var value: T? = null

            override fun component1(): T? = value

            override fun component2(): (T?) -> Unit = { value = it }
        }
    }
}

@Composable
fun <T> rememberPrevious(
    current: T,
    shouldUpdate: (prev: T?, curr: T) -> Boolean = { prev: T?, current: T -> prev != current },
): T {
    val ref = rememberRef<T>()

    // launched after render, so the current render will have the old value anyway
    SideEffect {
        if (shouldUpdate(ref.value, current)) {
            ref.value = current
        }
    }

    return ref.value ?: run {
        Log.v("stav: rememberPrevious trying to reutrn null!!!!!!!!!!!!!!!!!!!")
        current
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun rememberExoplayer(): PreloadingExoPlayer {
    val context = LocalContext.current
    return remember { PreloadingExoPlayer(context) }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    mediaSource: MediaSource,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    player: PreloadingExoPlayer = rememberExoplayer(),
    placeholder: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Cyan)
        )
    }
) {
    val media = rememberPrevious(current = mediaSource, shouldUpdate = { prev, curr ->
//        println("stav: prev=$prev curr=$curr (equals=${prev == curr})")
        if (prev != curr) {
            prev?.let {
                Log.e("stav: forgetting $it")
                player.forget(it)
            }
            Log.w("stav: preparing $curr")
            player.prepare(curr)
            true
        } else false
    })
    val mediaName = media.mediaItem.mediaId
    LaunchedEffect(isFocused) {
        println("stav: isFocused=$isFocused ($mediaName)")
//        if (isFocused) {
//            player.play(media)
//        }
    }
    //TODO: fix lifecycle stuff
    var lifecycle by remember { mutableStateOf(Lifecycle.Event.ON_CREATE) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            lifecycle = event
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
//            exoPlayer.release()
        }
    }
    var isPlaying by remember { mutableStateOf(false) }
    val listener = remember {
        object : Player.Listener {
            override fun onRenderedFirstFrame() {
//                println("stav: first frame rendered $tag")
            }

            override fun onIsPlayingChanged(_isPlaying: Boolean) {
                println("stav: isPlayingChanged: $_isPlaying $mediaName")
                isPlaying = _isPlaying
            }
        }
    }
    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {
                    useController = false
//                this.player = player.exoPlayer
                }
            },
            update = {
                if (isFocused) {
//                    player.setView(it, listener)
//                    player.play(media)
                    player.maybeSetAndPlay(it, listener, media)
                } else {
                    isPlaying = false
//                    player.maybeClearView(it)
//                    player.exoPlayer.removeListener(listener)
//                    it.player = null
                }
                when (lifecycle) {
                    Lifecycle.Event.ON_PAUSE -> {
                        it.onPause()
//                    exoPlayer.pause()
                    }

                    Lifecycle.Event.ON_RESUME -> {
                        it.onResume()
                    }

                    else -> {}
                }
            }
        )

        AnimatedVisibility(visible = !isPlaying, enter = fadeIn(), exit = fadeOut()) {
            placeholder()
        }
    }
}

//@SuppressLint("UnsafeOptInUsageError")
//@Composable
//fun VideoPlayer(
//    mediaSource: MediaSource,
//    modifier: Modifier = Modifier,
//    shouldPlay: Boolean = true,
//    tag: String = "",
//    placeholder: @Composable () -> Unit = {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Cyan)
//        )
//    }
//) {
//    Box(modifier) {
//
//        val context = LocalContext.current
//
//        var isPlaying by remember { mutableStateOf(false) }
//        // Do not recreate the player everytime this Composable commits
//        val exoPlayer = remember(mediaSource) {
//            println("stav: new player")
//            ExoPlayer.Builder(context)
//                .setPlaybackLooper(videoPlayerLooper)
////                .setRenderersFactory(
////                    DefaultRenderersFactory(context)
////                        .setEnableDecoderFallback(true)
////                )
////                .setLoadControl(
////                    DefaultLoadControl.Builder()
////                        .setPrioritizeTimeOverSizeThresholds(true)
////                        .setBufferDurationsMs(
////                        20000, 20000, 1000, 1000
////                    ).build()
////                )
//                .build()
//                .apply {
//                    setMediaSource(mediaSource)
//                    trackSelectionParameters = trackSelectionParameters.buildUpon()
//                        .setMaxVideoSizeSd()
////                    .setMaxVideoBitrate(3000)
//                        .build()
//                    repeatMode = Player.REPEAT_MODE_ALL
//                    volume = 0f // temp
////                    setImageOutput(object: ImageOutput{
////                        override fun onImageAvailable(presentationTimeUs: Long, bitmap: Bitmap) {
////                            println("stav: image available $tag")
////                        }
////
////                        override fun onDisabled() {
////                        }
////                    })
//                    repeatMode = Player.REPEAT_MODE_ALL
//                    playWhenReady = false
//                    addListener(object : Player.Listener {
//                        override fun onRenderedFirstFrame() {
//                            println("stav: first frame rendered $tag")
//                        }
//
//                        override fun onIsPlayingChanged(_isPlaying: Boolean) {
//                            println("stav: isPlayingChanged: $_isPlaying $tag (manifest=$currentManifest)")
//                            isPlaying = _isPlaying
//                            (currentManifest as? DashManifest)?.let { manifest ->
//                                manifest.getPeriod(0).adaptationSets.flatMap { it.representations }
//                                    .map { it.format.containerMimeType }
//                            }
//                        }
//                    })
//                    prepare()
//                }
//        }
//
//        LaunchedEffect(shouldPlay) {
//            exoPlayer.playWhenReady = shouldPlay
//        }
//        var lifecycle by remember {
//            mutableStateOf(Lifecycle.Event.ON_CREATE)
//        }
//        // Gateway to traditional Android Views
//        AndroidView(
//            modifier = Modifier.fillMaxSize(),
//            factory = { context ->
//                PlayerView(context).apply {
//                    useController = false
//                    player = exoPlayer
//                    (videoSurfaceView as? SurfaceView)?.let {
////                    PixelCopy.request(it,)
//
//                    }
//                }
//            },
//            update = {
//                when (lifecycle) {
//                    Lifecycle.Event.ON_PAUSE -> {
//                        it.onPause()
//                        exoPlayer.pause()
//                    }
//
//                    Lifecycle.Event.ON_RESUME -> {
//                        it.onResume()
//                    }
//
//                    else -> {}
//                }
//            }
//        )
//        val lifecycleOwner = LocalLifecycleOwner.current
//        DisposableEffect(Unit) {
//            val observer = LifecycleEventObserver { _, event ->
//                lifecycle = event
//            }
//            onDispose {
//                lifecycleOwner.lifecycle.removeObserver(observer)
//                exoPlayer.release()
//            }
//        }
//
//        AnimatedVisibility(visible = !isPlaying, enter = fadeIn(), exit = fadeOut()) {
//            placeholder()
//        }
//    }
//}

/**
 * Share a single Looper for all ExoPlayer instances.
 */
val videoPlayerLooper: Looper =
    HandlerThread("videoPlayerLooper", Process.THREAD_PRIORITY_AUDIO)
        .apply { start() }
        .looper
