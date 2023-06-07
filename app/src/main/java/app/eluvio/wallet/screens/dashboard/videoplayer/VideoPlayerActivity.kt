package app.eluvio.wallet.screens.dashboard.videoplayer

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import app.eluvio.wallet.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class VideoPlayerActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
    }
}
