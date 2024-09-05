package app.eluvio.wallet.data.stores

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.max

/**
 * Keeps track of the playback position for each media item.
 */
class PlaybackStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "playback_store", Context.MODE_PRIVATE
    )

    /**
     * Get the playback position for a media item.
     * Returns 0 if no position is stored.
     */
    fun getPlaybackPosition(mediaId: String): Long {
        val position = prefs.getLong(mediaId, 0)
        // Safeguard against bad (negative) values
        return max(0, position)
    }

    /**
     * Set the playback position for a media item.
     */
    fun setPlaybackPosition(mediaId: String, position: Long) {
        prefs.edit().putLong(mediaId, position).apply()
    }

    fun wipe() {
        prefs.edit().clear().apply()
    }
}
