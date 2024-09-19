package app.eluvio.wallet

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.util.Consumer
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.Surface
import app.eluvio.wallet.data.AspectRatio
import app.eluvio.wallet.data.entities.VideoOptionsEntity
import app.eluvio.wallet.navigation.ComposeNavigator
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.screens.NavGraphs
import app.eluvio.wallet.screens.common.MyMediaSource
import app.eluvio.wallet.screens.common.VideoPlayer
import app.eluvio.wallet.screens.common.rememberExoplayer
import app.eluvio.wallet.screens.common.videoPlayerLooper
import app.eluvio.wallet.screens.videoplayer.toMediaSource
import app.eluvio.wallet.theme.EluvioTheme
import app.eluvio.wallet.util.logging.Log
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
@OptIn(ExperimentalLayoutApi::class)
class MainActivity : ComponentActivity() {
    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val cache = SimpleCache(
//            cacheDir,
//            LeastRecentlyUsedCacheEvictor(50 * 1024 * 1024),
//            StandaloneDatabaseProvider(this)
//        )
//        val bunny = VideoOptionsEntity(
//            "dash",
//            "https://raw.githubusercontent.com/bbc/exoplayer-testing-samples/refs/heads/master/app/src/androidTest/assets/streams/files/bigbuckbunny/bigbuckbunny.mpd",
//            "clear",
//            null,
//            emptyMap()
//        ).toMediaSource(null)
//
//        val chan1 = VideoOptionsEntity(
//            "dash",
//            "https://livesim.dashif.org/livesim/chunkdur_1/ato_7/testpic4_8s/Manifest.mpd?channel=1",
//            "clear",
//            null,
//            emptyMap()
//        ).toMediaSource(null)
//
//        val elephant = VideoOptionsEntity(
//            "mp4",
//            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
//            "clear",
//            null,
//            emptyMap()
//        ).toMediaSource(null)
//
//        val sources = listOf(chan1, elephant).map { MyMediaSource(it) }

//        val exoPlayer = ExoPlayer.Builder(this)
//            .setAudioAttributes(AudioAttributes.DEFAULT,  /* handleAudioFocus= */true)
//            .setPlaybackLooper(videoPlayerLooper)
//            .build()
//            .apply {
//                playWhenReady = true
//                addMediaSources(sources)
//                prepare()
//            }
//        setContentView(R.layout.activity_main)
//        val pv1 = findViewById<PlayerView>(R.id.pv1)
//        pv1.player = exoPlayer
//        val pv2 = findViewById<PlayerView>(R.id.pv2)
//        val players = listOf(pv1, pv2)
//        var currentPlayer = pv1
//        findViewById<Button>(R.id.le_next).setOnClickListener {
//            val nextIndex = (exoPlayer.currentMediaItemIndex + 1) % exoPlayer.mediaItemCount
//            println(("stav: seeking to $nextIndex"))
//            exoPlayer.seekTo(nextIndex, C.TIME_UNSET)
//            currentPlayer.player = null
//            currentPlayer = players[nextIndex]
//            currentPlayer.player = exoPlayer
//        }
//        return

//        setContent {
////            var frame by remember { mutableStateOf<Bitmap?>(null) }
////            LaunchedEffect(Unit) {
////                launch(Dispatchers.IO) {
////                    try {
//////                    frame = FFmpegMediaMetadataRetriever().apply {
//////                        println("stav: starting...")
////                        val ret = FFmpegMediaMetadataRetriever()
//////                        val ret = MediaMetadataRetriever()
////                        ret.setDataSource(
//////                                this@MainActivity,
//////                            Uri.parse("https://livesim.dashif.org/livesim/chunkdur_1/ato_7/testpic4_8s/Manifest.mpd")
//////                                Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
//////                            "https://livesim.dashif.org/livesim/chunkdur_1/ato_7/testpic4_8s/Manifest.mpd",
////                            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
//////                            emptyMap()
////                        )
//////                        println("stav: setDataSource")
//////                        frame = ret.getFrameAtTime(-1, OPTION_CLOSEST)
//////                        println("stav: GOT IT")
//////                    }.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST)
////                    } catch (e: Exception) {
////                        e.printStackTrace()
////                    }
////                }
////            }
////            var i by remember { mutableIntStateOf(0) }
////            BackHandler {
////                i++
////            }
//            FlowRow {
////                frame?.let { frame ->
////                    println("stav: showing frame")
////                    Image(bitmap = frame.asImageBitmap(), contentDescription = null)
////                }
//                val player = rememberExoplayer()
//                repeat(sources.size) {
//                    val interactionSource = remember { MutableInteractionSource() }
//                    val isFocused by interactionSource.collectIsFocusedAsState()
//                    Surface(
//                        onClick = { /*TODO*/ },
//                        interactionSource = interactionSource,
//                        modifier = Modifier.padding(20.dp)
//                    ) {
//                        VideoPlayer(
//                            mediaSource = sources[it],
//                            isFocused = isFocused,
//                            player = player,
//                            modifier = Modifier
//                                .width(200.dp)
//                                .aspectRatio(AspectRatio.WIDE),
//                        )
//                    }
////                    VideoPlayer(
////                        mediaSource = media1,
////                        Modifier
////                            .width(200.dp)
////                            .aspectRatio(AspectRatio.WIDE),
//////                        shouldPlay = true,
////                        shouldPlay = i % total == it,
////                        tag = "#$it"
////                    )
//                }
//            }
//        }
//        return
        setContent {
            EluvioTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
//                        .background(Color(0xFF050505))
                ) {
                    Image(
                        painterResource(id = R.drawable.bg_gradient),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                    val navController = rememberNavController()
                    DisposableEffect(navController) {
                        val consumer = Consumer<Intent> {
                            Log.d("New intent captured and forwarded to navController: $it")
                            navController.handleDeepLink(it)
                        }
                        this@MainActivity.addOnNewIntentListener(consumer)
                        onDispose {
                            this@MainActivity.removeOnNewIntentListener(consumer)
                        }
                    }
                    val navigator = remember {
                        ComposeNavigator(
                            navController,
                            onBackPressedDispatcherOwner = this@MainActivity
                        )
                    }
                    CompositionLocalProvider(
                        LocalNavigator provides navigator
                    ) {
                        DestinationsNavHost(
                            navGraph = NavGraphs.root,
                            navController = navController,
                        )
                        if (BuildConfig.DEBUG) {
                            // Print nav backstack for debugging
                            // noinspection RestrictedApi
                            navController.currentBackStack.collectAsState().value.print()
                        }
                    }
                }
            }
        }
    }

    private val envSelectorHook by lazy { EnvSelectorHook(this) }
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return if (BuildConfig.DEBUG && envSelectorHook.onKeyUp(keyCode, event)) {
            true
        } else {
            super.onKeyUp(keyCode, event)
        }
    }
}

private fun Collection<NavBackStackEntry>.print(prefix: String = "navstack") {
    fun NavBackStackEntry.routeWithArgs(): String {
        val fallback = destination.route ?: ""
        return arguments?.keySet()?.fold(fallback) { route, key ->
            @Suppress("DEPRECATION")
            val value = arguments?.get(key)?.takeIf { it is String }?.toString() ?: "{$key}"
            route.replace("{$key}", value)
        } ?: fallback
    }

    val stack = map { it.routeWithArgs() }.toTypedArray().contentToString()
    Log.v("$prefix = $stack")
}

private class EnvSelectorHook(private val context: Context) {
    private val magicSequence = listOf(
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_LEFT,
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_DPAD_LEFT,
        KeyEvent.KEYCODE_DPAD_RIGHT,
    )

    private var index = 0

    fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP) {
            if (keyCode == magicSequence[index]) {
                index++
                if (index == magicSequence.size) {
                    index = 0
                    // Sequence completed. Launching the debug activity
                    context.startActivity(Intent().apply {
                        component = ComponentName(
                            "app.eluvio.wallet.debug",
                            "app.eluvio.wallet.debug.EnvSelectActivity"
                        )
                    })
                    return true
                }
            } else {
                index = 0
            }
        }
        return false
    }
}
