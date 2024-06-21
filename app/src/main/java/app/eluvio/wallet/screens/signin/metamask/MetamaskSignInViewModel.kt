package app.eluvio.wallet.screens.signin.metamask

import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.stores.MetamaskActivationStore
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.network.api.authd.MetamaskActivationData
import app.eluvio.wallet.screens.NavGraphs
import app.eluvio.wallet.screens.common.generateQrCode
import app.eluvio.wallet.screens.signin.SignInViewModel
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.rx.interval
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.Flowables
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class MetamaskSignInViewModel @Inject constructor(
    private val metamaskActivationStore: MetamaskActivationStore,
) : BaseViewModel<SignInViewModel.State>(SignInViewModel.State()) {

    private var activationDataDisposable: Disposable? = null
    private var activationCompleteDisposable: Disposable? = null

    override fun onResume() {
        super.onResume()

        observeActivationData()
    }

    fun requestNewToken() {
        observeActivationData()
    }

    private fun observeActivationData() {
        activationDataDisposable?.dispose()
        activationDataDisposable = metamaskActivationStore.observeMetamaskActivationData()
            .doOnNext {
                observeActivationComplete(it)
            }
            .switchMapSingle { activationData ->
                generateQrCode(activationData.metamaskUrl)
                    .map { qr -> activationData to qr }
            }
            .subscribeBy { (activationData, qrCode) ->
                updateState {
                    copy(
                        qrCode = qrCode,
                        userCode = activationData.code,
                        loading = false
                    )
                }
            }
            .addTo(disposables)
    }

    private fun observeActivationComplete(activationData: MetamaskActivationData) {
        activationCompleteDisposable?.dispose()
        activationCompleteDisposable =
            Flowables.interval(5.seconds)
                .doOnSubscribe {
                    Log.d("starting to poll token for code=${activationData.code}")
                }
                .flatMapMaybe { metamaskActivationStore.checkToken(activationData) }
                .firstOrError(
                    // stop the interval as soon as [checkToken] returns a non-null value
                )
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
                    }
                )
                .addTo(disposables)
    }
}
