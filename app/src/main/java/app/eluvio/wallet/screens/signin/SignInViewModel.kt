package app.eluvio.wallet.screens.signin

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.AuthenticationService
import app.eluvio.wallet.data.stores.DeviceActivationStore
import app.eluvio.wallet.navigation.asNewRoot
import app.eluvio.wallet.network.DeviceActivationData
import app.eluvio.wallet.screens.NavGraphs
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.mapNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.g0dkar.qrcode.QRCode
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val deviceActivationStore: DeviceActivationStore,
    private val authenticationService: AuthenticationService,
) : BaseViewModel<SignInViewModel.State>(State()) {
    data class State(
        val loading: Boolean = true,
        val qrCode: Bitmap? = null,
        val userCode: String? = null,
        val url: String? = null,
    )

    private var activationDataDisposable: Disposable? = null
    private var activationCompleteDisposable: Disposable? = null

    fun requestNewToken(qrSize: Int) {
        observeActivationData(qrSize)
    }

    private fun observeActivationData(qrSize: Int) {
        activationDataDisposable?.dispose()
        activationDataDisposable = deviceActivationStore.observeActivationData()
            .doOnNext {
                observeActivationComplete(it)
            }
            .switchMapSingle { activationData ->
                generateQrCode(activationData.verificationUriComplete, qrSize)
                    .map { qr -> activationData to qr }
            }
            .subscribeBy { (activationData, qrCode) ->
                updateState {
                    copy(
                        qrCode = qrCode,
                        url = activationData.verificationUri,
                        userCode = activationData.userCode,
                        loading = false
                    )
                }
            }
            .addTo(disposables)
    }

    private fun generateQrCode(url: String, size: Int): Single<Bitmap> {
        return Single.create {
            val bytes = ByteArrayOutputStream()
            val qr = QRCode(url)
            val rawData = qr.encode()
            val margin = 20 //(pixels)
            val cellSize = (size - margin) / rawData.size
            qr.render(margin = margin, cellSize = cellSize, rawData = rawData).writeImage(bytes)
            val bitmap = BitmapFactory.decodeByteArray(bytes.toByteArray(), 0, bytes.size())
            Log.d("QR generated for url: $url")
            it.onSuccess(bitmap)
        }
    }

    private fun observeActivationComplete(activationData: DeviceActivationData) {
        activationCompleteDisposable?.dispose()
        activationCompleteDisposable =
            Observable.interval(activationData.intervalSeconds, TimeUnit.SECONDS)
                .doOnSubscribe {
                    Log.d("starting to poll token for userCode=${activationData.userCode} (intervalSeconds=${activationData.intervalSeconds})")
                }
                .flatMapSingle { deviceActivationStore.checkToken(activationData.deviceCode) }
                .mapNotNull { it.body() }
                .firstOrError(
                    // stop the interval as soon as [checkToken] returns a non-null value
                )
                .flatMap {
                    // Now that we have an idToken, we can get a fabricToken.
                    // this includes some local crypto magic, as well as a sign request from the server.
                    authenticationService.getFabricToken(it.idToken)
                }
                .doOnError {
                    Log.e(
                        "Activation polling error! This shouldn't happen, restarting polling.",
                        it
                    )
                }
                .retry()
                .subscribeBy(
                    onSuccess = {
                        Log.d("Got a token $it")
                        navigateTo(NavGraphs.mainGraph.asNewRoot())
                    }
                )
                .addTo(disposables)
    }
}
