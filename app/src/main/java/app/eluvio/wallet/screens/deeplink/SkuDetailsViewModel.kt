package app.eluvio.wallet.screens.deeplink

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.screens.destinations.SkuDetailsDestination
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class SkuDetailsViewModel @Inject constructor(
    private val contentStore: ContentStore,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<SkuDetailsViewModel.State>(State()) {
    data class State(val loading: Boolean = true, val owned: Boolean = false)

    private val navArgs = SkuDetailsDestination.argsFrom(savedStateHandle)
    override fun onResume() {
        super.onResume()

        contentStore.observeWalletData(forceRefresh = true)
            .withLatestFrom(
                contentStore.observerNftBySku(
                    navArgs.marketplace,
                    navArgs.sku
                ).startWithItem(Result.failure(Exception("first")))
            ) { nfts, sku ->
                sku.getOrNull()?.let { sku ->
                    val mediaForSku =
                        nfts.getOrNull()?.firstOrNull { it.contractAddress == sku.contractAddress }
                    updateState { copy(loading = false, owned = mediaForSku != null) }
                    if (mediaForSku != null) {
                        Log.w("stav: user owns SKU")
                    } else {
                        Log.w("stav: user Doesn't own SKU")
                    }
                }
                nfts
            }
            .subscribeBy(
                onNext = {  },
                onError = { Log.e("Error getting wallet data", it) }
            )
            .addTo(disposables)
    }
}
