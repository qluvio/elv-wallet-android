package app.eluvio.wallet.screens.signin

import android.graphics.Bitmap
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.AfterSignInDestination
import app.eluvio.wallet.data.AuthenticationService
import app.eluvio.wallet.data.stores.DeviceActivationStore
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.network.api.DeviceActivationData
import app.eluvio.wallet.screens.NavGraphs
import app.eluvio.wallet.screens.common.generateQrCode
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.rx.mapNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
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
    )

    private var activationDataDisposable: Disposable? = null
    private var activationCompleteDisposable: Disposable? = null
    private var lastQrSizeRequested: Int? = null

    override fun onResume() {
        super.onResume()
        lastQrSizeRequested?.let {
            observeActivationData(it)
        }
    }

    fun requestNewToken(qrSize: Int) {
        lastQrSizeRequested = qrSize
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
                        userCode = activationData.userCode,
                        loading = false
                    )
                }
            }
            .addTo(disposables)
    }

    private fun observeActivationComplete(activationData: DeviceActivationData) {
        activationCompleteDisposable?.dispose()
        activationCompleteDisposable =
            Flowable.interval(activationData.intervalSeconds, TimeUnit.SECONDS)
                .doOnSubscribe {
                    Log.d("starting to poll token for userCode=${activationData.userCode} (intervalSeconds=${activationData.intervalSeconds})")
                }
                .flatMapSingle { deviceActivationStore.checkToken(activationData.deviceCode) }
                .mapNotNull { it.body() }
                .firstOrError(
                    // stop the interval as soon as [checkToken] returns a non-null value
                )
                .flatMap {
                    // Now that we have an idToken (embedded in headers by retrofit interceptors), we can get a fabricToken.
                    // this includes some local crypto magic, as well as a sign request from the server.
                    authenticationService.getFabricToken()
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
                        navigateTo(NavigationEvent.PopTo(NavGraphs.authFlowGraph, true))
                        val nextDestination = AfterSignInDestination.direction.getAndSet(null)
                        if (nextDestination != null) {
                            navigateTo(NavigationEvent.Push(nextDestination))
                        }
                    }
                )
                .addTo(disposables)
    }
}
