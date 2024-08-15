package app.eluvio.wallet.screens.signin.common

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.AfterSignInDestination
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.screens.NavGraphs
import app.eluvio.wallet.screens.common.generateQrCode
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.rx.interval
import app.eluvio.wallet.util.rx.mapNotNull
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.Flowables
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlin.time.Duration

/**
 * Authentication is extremely similar between all providers, so this class does the heavy lifting
 * and only delegates the provider-specific logic to the subclasses.
 */
abstract class BaseLoginViewModel<ActivationData : Any>(
    // The bg image / logo will be fetched from the property, if available
    private val propertyId: String?,
    // Just for convenience, we always provide this, even if we don't use it (propertyId=null)
    private val propertyStore: MediaPropertyStore,
    private val apiProvider: ApiProvider,
    savedStateHandle: SavedStateHandle? = null
) : BaseViewModel<LoginState>(LoginState(), savedStateHandle) {

    abstract fun fetchActivationDate(): Flowable<ActivationData>

    /**
     * Check if token has been activated and handle the result.
     * The [Maybe] should only emit a value when authentication is successful.
     */
    abstract fun ActivationData.checkToken(): Maybe<*>
    abstract fun ActivationData.getPollingInterval(): Duration
    abstract fun ActivationData.getQrUrl(): String
    abstract fun ActivationData.getCode(): String


    private var activationDataDisposable: Disposable? = null
    private var activationCompleteDisposable: Disposable? = null

    override fun onResume() {
        super.onResume()
        observeActivationData()

        propertyId?.let {
            apiProvider.getFabricEndpoint()
                .flatMapPublisher { baseUrl ->
                    propertyStore.observeMediaProperty(propertyId, forceRefresh = false)
                        .mapNotNull { property ->
                            property.mainPage?.backgroundImagePath?.let { "$baseUrl$it" }
                        }
                }
                .subscribeBy {
                    updateState { copy(bgImageUrl = it) }
                }
        }
    }

    fun requestNewToken() {
        observeActivationData()
    }

    private fun observeActivationData() {
        activationDataDisposable?.dispose()
        activationDataDisposable = fetchActivationDate()
            .doOnNext {
                observeActivationComplete(it)
            }
            .switchMapSingle { activationData ->
                generateQrCode(activationData.getQrUrl())
                    .map { qr -> activationData to qr }
            }
            .subscribeBy { (activationData, qrCode) ->
                updateState {
                    copy(
                        qrCode = qrCode,
                        userCode = activationData.getCode(),
                        loading = false
                    )
                }
            }
            .addTo(disposables)
    }

    private fun observeActivationComplete(activationData: ActivationData) {
        activationCompleteDisposable?.dispose()
        activationCompleteDisposable =
            Flowables.interval(activationData.getPollingInterval())
                .doOnSubscribe {
                    Log.d("starting to poll token for code=${activationData.getCode()}")
                }
                .flatMapMaybe { activationData.checkToken() }
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
                        val nextDestination = AfterSignInDestination.direction.getAndSet(null)
                        if (nextDestination != null) {
                            navigateTo(NavigationEvent.Push(nextDestination))
                        }
                    }
                )
                .addTo(disposables)
    }
}
