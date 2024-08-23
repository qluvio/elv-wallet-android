package app.eluvio.wallet.screens.purchaseprompt

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.MediaEntity
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
    private val propertyStore: MediaPropertyStore,
    private val apiProvider: ApiProvider,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<PurchasePromptViewModel.State>(State(), savedStateHandle) {

    @Immutable
    data class State(
        val media: MediaEntity? = null,
        val itemPurchase: State.ItemPurchase? = null,
        val qrImage: Bitmap? = null,
        val bgImageUrl: String? = null
    ) {
        @Immutable
        data class ItemPurchase(val title: String, val subtitle: String?, val image: String?)
    }

    private val navArgs = savedStateHandle.navArgs<PurchasePromptNavArgs>()

    override fun onResume() {
        super.onResume()

        // TODO: swap with real purchase link
        generateQrCode("https://eluv.io")
            .subscribeBy { updateState { copy(qrImage = it) } }
            .addTo(disposables)

        apiProvider.getFabricEndpoint().flatMapPublisher { baseUrl ->
            propertyStore.observeMediaProperty(navArgs.propertyId)
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

        propertyStore.getSectionItem(navArgs.sectionItemId)
            .subscribeBy(onSuccess = { sectionItem ->
                val itemPurchase = sectionItem.purchaseOptionsUrl?.let {
                    State.ItemPurchase(
                        title = sectionItem.title ?: "",
                        subtitle = sectionItem.subtitle,
                        image = sectionItem.thumbnailUrl
                    )
                }
                updateState { copy(media = sectionItem.media, itemPurchase = itemPurchase) }
            })
            .addTo(disposables)
    }
}
