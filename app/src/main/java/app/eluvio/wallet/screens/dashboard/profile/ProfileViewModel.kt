package app.eluvio.wallet.screens.dashboard.profile

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.SignOutHandler
import app.eluvio.wallet.data.stores.Environment
import app.eluvio.wallet.data.stores.EnvironmentStore
import app.eluvio.wallet.data.stores.FabricConfigStore
import app.eluvio.wallet.data.stores.TokenStore
import app.eluvio.wallet.util.base58
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.Flowables
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tokenStore: TokenStore,
    private val fabricConfigStore: FabricConfigStore,
    private val environmentStore: EnvironmentStore,
    private val signOutHandler: SignOutHandler,
) : BaseViewModel<ProfileViewModel.State>(State(), savedStateHandle) {

    @Parcelize
    data class State(
        val address: String = "",
        val userId: String = "",
        val network: Environment? = null,
        val fabricNode: String = "",
        val authNode: String = "",
        val ethNode: String = "",
    ) : Parcelable

    override fun onResume() {
        super.onResume()
        Flowables.combineLatest(
            environmentStore.observeSelectedEnvironment(),
            fabricConfigStore.observeFabricConfiguration(),
            tokenStore.walletAddress.observe(),
        ).subscribeBy { (env, config, walletAddress) ->
            val address = walletAddress.orDefault(null) ?: ""
            updateState {
                copy(
                    address = address,
                    userId = "iusr${address.base58}",
                    network = env,
                    fabricNode = config.fabricEndpoint,
                    authNode = config.authdEndpoint,
                    ethNode = config.network.services.ethereumApi.first(),
                )
            }
        }
            .addTo(disposables)
    }

    fun signOut() {
        signOutHandler.signOut("Sign out successful")
            // No action needed. SignOutHandler takes care of restarting the app
            .subscribeBy(onError = { Log.e("error logging out", it) })
            .addTo(disposables)
    }
}
