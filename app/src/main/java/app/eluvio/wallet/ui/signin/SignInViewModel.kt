package app.eluvio.wallet.ui.signin

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.AuthenticationService
import app.eluvio.wallet.data.DeviceActivationStore
import app.eluvio.wallet.navigation.Screen
import app.eluvio.wallet.network.DeviceActivationData
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.mapNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.g0dkar.qrcode.QRCode
import io.reactivex.rxjava3.core.Completable
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

    override fun onStart() {
        super.onStart()
        observeActivationData()
    }

    fun requestNewToken() {
        observeActivationData()
    }

    private fun observeActivationData() {
        activationDataDisposable?.dispose()
        activationDataDisposable = deviceActivationStore.observeActivationData()
            .doOnNext {
                observeActivationComplete(it)
            }
            .switchMapSingle { activationData ->
                generateQrCode(activationData.verificationUriComplete)
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

    private fun generateQrCode(url: String): Single<Bitmap> {
        return Single.create {
            val bytes = ByteArrayOutputStream()
            QRCode(url).render(margin = 20).writeImage(bytes)
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
                        navigateTo(Screen.MediaGallery)
                    }
                )
                .addTo(disposables)
    }

    private fun awaitActivationComplete(activationData: DeviceActivationData): Completable {
        return Observable.interval(activationData.intervalSeconds, TimeUnit.SECONDS)
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
            .doOnSuccess {
                Log.d("Got a token $it")
                navigateTo(Screen.MediaGallery)
            }
            .doOnError {
                Log.e("activation polling error", it)
            }
            .ignoreElement()
    }
}
