package app.eluvio.wallet.screens.signin.metamask

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.data.stores.MetamaskActivationStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.network.api.authd.MetamaskActivationData
import app.eluvio.wallet.screens.signin.common.BaseLoginViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class MetamaskSignInViewModel @Inject constructor(
    private val metamaskActivationStore: MetamaskActivationStore,
    propertyStore: MediaPropertyStore,
    apiProvider: ApiProvider,
    savedStateHandle: SavedStateHandle
) : BaseLoginViewModel<MetamaskActivationData>(
    propertyId = null, // We don't want the property bg for Metamask, so we pass null
    propertyStore,
    apiProvider,
    savedStateHandle
) {
    override fun fetchActivationData(): Flowable<MetamaskActivationData> =
        metamaskActivationStore.observeMetamaskActivationData()

    override fun MetamaskActivationData.checkToken(): Maybe<*> =
        metamaskActivationStore.checkToken(this)

    override fun MetamaskActivationData.getCode(): String = code

    override fun MetamaskActivationData.getQrUrl(): String = metamaskUrl

    override fun MetamaskActivationData.getPollingInterval(): Duration = 5.seconds
}
