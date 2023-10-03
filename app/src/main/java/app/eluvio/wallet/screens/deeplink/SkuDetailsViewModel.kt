package app.eluvio.wallet.screens.deeplink

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.navigation.asReplace
import app.eluvio.wallet.screens.dashboard.myitems.AllMediaProvider
import app.eluvio.wallet.screens.destinations.NftDetailDestination
import app.eluvio.wallet.screens.destinations.SkuDetailsDestination
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class SkuDetailsViewModel @Inject constructor(
    private val contentStore: ContentStore,
    private val allMediaProvider: AllMediaProvider,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<SkuDetailsViewModel.State>(State()) {
    data class State(
        val loading: Boolean = true,
        val media: AllMediaProvider.Media? = null
    )

    private val navArgs = SkuDetailsDestination.argsFrom(savedStateHandle)
    override fun onResume() {
        super.onResume()

        allMediaProvider.observeAllMedia(onNetworkError = { })
            .withLatestFrom(
                contentStore.observerNftBySku(
                    navArgs.marketplace,
                    navArgs.sku
                ).startWithItem(Result.failure(Exception("first")))
            ) { allMedia, templateResult ->
                templateResult.getOrNull()?.let { nftTemplate ->
                    val mediaForSku =
                        allMedia.media.firstOrNull { it.contractAddress == nftTemplate.contractAddress }
                    if (mediaForSku?.tokenId != null) {
                        Log.w("user owns SKU")
                        navigateTo(
                            NftDetailDestination(
                                mediaForSku.contractAddress,
                                mediaForSku.tokenId
                            ).asReplace()
                        )
                    } else {
                        Log.w("user Doesn't own SKU")
                        updateState {
                            copy(
                                loading = false,
                                media = AllMediaProvider.Media.fromTemplate(nftTemplate)
                            )
                        }
                    }
                }
                allMedia
            }
            .subscribeBy(
                onNext = { },
                onError = { Log.e("Error getting wallet data", it) }
            )
            .addTo(disposables)
    }
}
