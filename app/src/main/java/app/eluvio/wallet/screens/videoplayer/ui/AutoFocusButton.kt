package app.eluvio.wallet.screens.videoplayer.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton

/**
 * A button that requests focus when enabled.
 * This is needed because [PlayerView] will try to focus the play button when the controller is shown,
 * but it might not be enabled yet, because we're still buffering the content.
 */
class AutoFocusButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ImageButton(context, attrs) {
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (enabled && visibility == VISIBLE) {
            requestFocus()
        }
    }
}
