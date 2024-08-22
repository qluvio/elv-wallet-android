package app.eluvio.wallet.screens.nftdetail

import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.screens.dashboard.myitems.AllMediaProvider
import app.eluvio.wallet.util.rx.mapNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class NftDetailViewModel @Inject constructor(
    private val navArgs: NftDetailNavArgs,
    private val contentStore: ContentStore,
) : BaseViewModel<NftDetailViewModel.State>(State()) {

    data class State(val media: AllMediaProvider.Media? = null)

    override fun onResume() {
        super.onResume()

        contentStore.observeNft(navArgs.contractAddress, navArgs.tokenId)
            .mapNotNull { entity ->
                entity.nftTemplate?.let {
                    AllMediaProvider.Media.fromTemplate(it, entity.imageUrl, entity.tokenId)
                }
            }
            .subscribeBy { updateState { copy(media = it) } }
            .addTo(disposables)
    }
}
