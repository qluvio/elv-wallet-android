package app.eluvio.wallet.screens.videoplayer

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import app.eluvio.wallet.R
import app.eluvio.wallet.navigation.MainGraph
import com.ramcosta.composedestinations.annotation.ActivityDestination
import dagger.hilt.android.AndroidEntryPoint

@MainGraph
@ActivityDestination(navArgsDelegate = VideoPlayerArgs::class)
@AndroidEntryPoint
class VideoPlayerActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
    }
}
