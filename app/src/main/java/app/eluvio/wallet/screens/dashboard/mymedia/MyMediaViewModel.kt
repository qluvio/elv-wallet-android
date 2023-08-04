package app.eluvio.wallet.screens.dashboard.mymedia

import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.app.Events
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.screens.dashboard.myitems.AllMediaProvider
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.rx.mapNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class MyMediaViewModel @Inject constructor(
    private val contentStore: ContentStore,
    private val allMediaProvider: AllMediaProvider,
    private val apiProvider: ApiProvider,
) : BaseViewModel<MyMediaViewModel.State>(State()) {
    data class State(
        val featuredMedia: List<MediaEntity> = emptyList(),
        val nftMedia: Map<String, List<MediaEntity>> = emptyMap(),
        val myItems: List<AllMediaProvider.State.Media> = emptyList(),
        val baseUrl: String? = null,
    )

    override fun onResume() {
        super.onResume()

        apiProvider.getFabricEndpoint()
            .subscribeBy(
                onSuccess = { updateState { copy(baseUrl = it) } },
                onError = { Log.e("Error getting fabric endpoint", it) }
            )
            .addTo(disposables)

        contentStore.observeWalletData(forceRefresh = false)
            .mapNotNull { it.getOrNull() }
            .subscribeBy(
                onNext = { nfts ->
                    val featuredMedia = nfts.flatMap { it.featuredMedia }.distinct()
                    val nftMedia = nfts.associate { nft ->
                        nft.displayName to nft.mediaSections.flatMap { it.collections }
                            .flatMap { it.media }
                    }
                    updateState {
                        copy(
                            featuredMedia = featuredMedia,
                            nftMedia = nftMedia,
                        )
                    }
                },
                onError = { Log.e("Error getting wallet data", it) }
            )
            .addTo(disposables)

        allMediaProvider.observeAllMedia(onNetworkError = { fireEvent(Events.NetworkError) })
            .subscribeBy(
                onNext = { updateState { copy(myItems = it.media) } },
                onError = { Log.e("Error getting MyItems row in MyMedia", it) }
            )
            .addTo(disposables)
    }
}
