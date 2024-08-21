package app.eluvio.wallet.screens.purchaseprompt

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.screens.common.generateQrCode
import app.eluvio.wallet.screens.navArgs
import app.eluvio.wallet.util.rx.mapNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class PurchasePromptViewModel @Inject constructor(
    private val contentStore: ContentStore,
    private val propertyStore: MediaPropertyStore,
    private val apiProvider: ApiProvider,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<PurchasePromptViewModel.State>(State(), savedStateHandle) {

    @Immutable
    data class State(
        val media: MediaEntity? = null,
        val qrImage: Bitmap? = null,
        val bgImageUrl: String? = null
    )

    private val navArgs = savedStateHandle.navArgs<PurchasePromptNavArgs>()

    override fun onResume() {
        super.onResume()

        // TODO: swap with real purchase link
        generateQrCode("https://eluv.io")
            .subscribeBy { updateState { copy(qrImage = it) } }
            .addTo(disposables)

        navArgs.propertyId?.let { propertyId ->
            apiProvider.getFabricEndpoint().flatMapPublisher { baseUrl ->
                propertyStore.observeMediaProperty(propertyId)
                    .mapNotNull { property ->
                        property.mainPage?.backgroundImagePath
                            ?.takeIf { it.isNotEmpty() }
                            ?.let { "${baseUrl}${it}" }
                    }
            }
                .subscribeBy(
                    onNext = { updateState { copy(bgImageUrl = it) } },
                    onError = {}
                )
                .addTo(disposables)
        }

        contentStore.observeMediaItem(navArgs.mediaId)
            .subscribeBy(
                onNext = { updateState { copy(media = it) } },
                onError = {}
            )
            .addTo(disposables)
    }
}
