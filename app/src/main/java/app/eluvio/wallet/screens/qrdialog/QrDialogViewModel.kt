package app.eluvio.wallet.screens.qrdialog

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.data.stores.TokenStore
import app.eluvio.wallet.screens.common.generateQrCode
import app.eluvio.wallet.screens.destinations.QrDialogDestination
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class QrDialogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contentStore: ContentStore,
    private val tokenStore: TokenStore,
) : BaseViewModel<QrDialogViewModel.State>(State()) {
    data class State(val qrCode: Bitmap? = null, val error: Boolean = false)

    private val mediaId = QrDialogDestination.argsFrom(savedStateHandle).mediaItemId
    override fun onResume() {
        super.onResume()
        contentStore.observeMediaItem(mediaId)
            .switchMapSingle {
                generateQrCode(getAuthorizedUrl(it), 512)
            }
            .subscribeBy(
                onNext = {
                    updateState { copy(qrCode = it, error = false) }
                },
                onError = {
                    updateState { copy(error = true) }
                    Log.e("Error loading QR for media item:$mediaId", it)
                }
            )
            .addTo(disposables)
    }

    private fun getAuthorizedUrl(media: MediaEntity): String {
        val url = Uri.parse(media.mediaFile.takeIf { it.isNotEmpty() }
            ?: media.mediaLinks.values.first())
        val token = tokenStore.fabricToken
        return if (token != null && url.getQueryParameter("authorization") == null) {
            url.buildUpon()
                .appendQueryParameter("authorization", token)
                .build()
                .toString()
        } else {
            url.toString()
        }
    }
}
