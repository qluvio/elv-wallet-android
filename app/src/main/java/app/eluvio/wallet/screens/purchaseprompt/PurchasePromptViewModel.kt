package app.eluvio.wallet.screens.purchaseprompt

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.v2.permissions.PermissionContext
import app.eluvio.wallet.data.entities.v2.permissions.toPurchaseUrl
import app.eluvio.wallet.data.stores.EnvironmentStore
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.screens.common.generateQrCode
import app.eluvio.wallet.screens.navArgs
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.rx.mapNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class PurchasePromptViewModel @Inject constructor(
    private val propertyStore: MediaPropertyStore,
    private val apiProvider: ApiProvider,
    private val environmentStore: EnvironmentStore,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<PurchasePromptViewModel.State>(State(), savedStateHandle) {

    @Immutable
    data class State(
        val media: MediaEntity? = null,
        val itemPurchase: ItemPurchase? = null,
        val qrImage: Bitmap? = null,
        val bgImageUrl: String? = null
    ) {
        @Immutable
        data class ItemPurchase(val title: String, val subtitle: String?, val image: String?)
    }

    private val permissionContext = savedStateHandle.navArgs<PermissionContext>()

    override fun onResume() {
        super.onResume()

        environmentStore.observeSelectedEnvironment()
            .firstOrError()
            .flatMap { env ->
                generateQrCode(permissionContext.toPurchaseUrl(env))
            }
            .subscribeBy(
                onSuccess = { updateState { copy(qrImage = it) } },
                onError = { Log.e("Error generating QR code", it) }
            )
            .addTo(disposables)

        apiProvider.getFabricEndpoint().flatMapPublisher { baseUrl ->
            propertyStore.observeMediaProperty(permissionContext.propertyId)
                .mapNotNull { property ->
                    property.mainPage?.backgroundImagePath
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { "${baseUrl}${it}" }
                }
        }
            .subscribeBy(
                onNext = { updateState { copy(bgImageUrl = it) } },
                onError = { Log.e("Error loading background image", it) }
            )
            .addTo(disposables)

        permissionContext.sectionItemId?.let {
            propertyStore.getSectionItem(it)
                .subscribeBy(
                    onSuccess = { sectionItem ->
                        val itemPurchase = State.ItemPurchase(
                            title = sectionItem.title ?: "",
                            subtitle = sectionItem.subtitle,
                            image = sectionItem.thumbnailUrl
                        )
                        updateState { copy(media = sectionItem.media, itemPurchase = itemPurchase) }
                    },
                    onError = { Log.e("Error loading purchase item", it) }
                )
                .addTo(disposables)
        }
    }
}
