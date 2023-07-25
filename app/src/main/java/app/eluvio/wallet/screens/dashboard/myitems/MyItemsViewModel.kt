package app.eluvio.wallet.screens.dashboard.myitems

import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.app.Events
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class MyItemsViewModel @Inject constructor(
    private val allMediaProvider: AllMediaProvider,
) : BaseViewModel<AllMediaProvider.State>(AllMediaProvider.State()) {

    override fun onResume() {
        super.onResume()
        allMediaProvider.observeAllMedia(onNetworkError = { fireEvent(Events.NetworkError) })
            .subscribeBy(
                onNext = { newState -> updateState { newState } },
                onError = { Log.e("Error getting wallet data", it) }
            )
            .addTo(disposables)
    }
}

