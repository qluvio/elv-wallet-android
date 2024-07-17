package app.eluvio.wallet.screens.signin.preamble

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import app.eluvio.wallet.BuildConfig
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.stores.Environment
import app.eluvio.wallet.data.stores.EnvironmentStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.kotlin.addTo
import javax.inject.Inject

@HiltViewModel
class SignInPreambleViewModel @Inject constructor(
    private val environmentStore: EnvironmentStore,
    @ApplicationContext private val context: Context,
) : BaseViewModel<SignInPreambleViewModel.State>(State()) {

    private val player = ExoPlayer.Builder(context)
        .setAudioAttributes(AudioAttributes.DEFAULT,  /* handleAudioFocus= */true)
        .build()
        .apply {
            addMediaItem(MediaItem.fromUri(Uri.parse("asset:///DiscoverNoText-1920x1080.webm")))
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true
            prepare()
        }

    @Immutable
    data class State(
        val loading: Boolean = true,
        val selectedEnvironment: Environment? = null,
        val availableEnvironments: List<Environment> = if (BuildConfig.DEBUG) {
            Environment.entries
        } else {
            listOf(Environment.Main)
        },
        val player: Player? = null
    )

    override fun onResume() {
        super.onResume()

        updateState {
            copy(player = this@SignInPreambleViewModel.player)
        }

        environmentStore.observeSelectedEnvironment()
            .subscribe { newEnv ->
                updateState {
                    copy(loading = false, selectedEnvironment = newEnv)
                }
            }
            .addTo(disposables)
    }

    override fun onCleared() {
        super.onCleared()

        player.release()
    }

    fun selectEnvironment(environment: Environment) {
        environmentStore.setSelectedEnvironment(environment)
    }
}
