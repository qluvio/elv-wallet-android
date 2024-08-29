package app.eluvio.wallet.screens.signin.auth0

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.data.AuthenticationService
import app.eluvio.wallet.data.stores.DeviceActivationStore
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.network.api.DeviceActivationData
import app.eluvio.wallet.screens.navArgs
import app.eluvio.wallet.screens.signin.common.BaseLoginViewModel
import app.eluvio.wallet.util.rx.mapNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class Auth0SignInViewModel @Inject constructor(
    private val deviceActivationStore: DeviceActivationStore,
    private val authenticationService: AuthenticationService,
    propertyStore: MediaPropertyStore,
    apiProvider: ApiProvider,
    savedStateHandle: SavedStateHandle
) : BaseLoginViewModel<DeviceActivationData>(
    savedStateHandle.navArgs<Auth0SignInNavArgs>().propertyId,
    propertyStore,
    apiProvider,
    savedStateHandle
) {
    override fun fetchActivationData(): Flowable<DeviceActivationData> =
        deviceActivationStore.observeActivationData().toFlowable(BackpressureStrategy.BUFFER)

    override fun DeviceActivationData.getPollingInterval(): Duration = intervalSeconds.seconds

    override fun DeviceActivationData.getQrUrl(): String = verificationUriComplete

    override fun DeviceActivationData.getCode(): String = userCode

    override fun DeviceActivationData.checkToken(): Maybe<*> =
        deviceActivationStore.checkToken(deviceCode)
            .mapNotNull { it.body() }
            .flatMapSingle { authenticationService.getFabricToken() }
}
