package app.eluvio.wallet.screens.purchaseprompt

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.app.Events
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.v2.permissions.PermissionContext
import app.eluvio.wallet.data.entities.v2.permissions.toPurchaseUrl
import app.eluvio.wallet.data.stores.EnvironmentStore
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.screens.common.generateQrCode
import app.eluvio.wallet.screens.navArgs
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.rx.asSharedState
import app.eluvio.wallet.util.rx.mapNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.util.concurrent.TimeUnit
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

    private val property = propertyStore
        .observeMediaProperty(permissionContext.propertyId, forceRefresh = false)
        .asSharedState()

    // Only available when pageId is provided. Otherwise, it's null.
    private val pageAndProperty = permissionContext.pageId
        ?.let { pageId ->
            property.switchMap { property ->
                propertyStore.observePage(property, pageId, forceRefresh = false)
                    .map { page -> property to page }
            }
        }
        ?.asSharedState()

    private val sectionItem by lazy {
        val sectionId = permissionContext.sectionId ?: return@lazy null
        val sectionItemId = permissionContext.sectionItemId ?: return@lazy null
        pageAndProperty?.switchMap { (property, page) ->
            propertyStore.observeSections(property, page, forceRefresh = false)
        }
            ?.mapNotNull { sections ->
                sections.firstOrNull { it.id == sectionId }
                    ?.items
                    ?.firstOrNull { it.id == sectionItemId }
            }
            ?.asSharedState()
    }

    override fun onResume() {
        super.onResume()

        updateQrCode()

        updateBackgroundImage()

        updateCardItem()

        pollForPermissionGranted()
    }

    private fun updateQrCode() {
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
    }

    private fun updateBackgroundImage(): Completable {
        // If no "page" is defined, default to the property's main page.
        val pageAndProperty =
            pageAndProperty ?: property.map { property -> property to property.mainPage!! }

        return pageAndProperty.flatMapMaybe { (property, page) ->
            apiProvider.getFabricEndpoint()
                .mapNotNull { baseUrl ->
                    val path = property.loginInfo?.backgroundImagePath?.ifEmpty { null }
                        ?: page.backgroundImagePath?.ifEmpty { null }
                    path?.let { "${baseUrl}${it}" }
                }
        }
            .doOnNext { updateState { copy(bgImageUrl = it) } }
            .doOnError { Log.e("Error loading background image", it) }
            .ignoreElements()
    }

    private fun updateCardItem() {
        sectionItem?.firstOrError()
            ?.subscribeBy(
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
            ?.addTo(disposables)
    }

    /**
     * Keep checking permissions to see if the user is now allowed to view the content,
     * then navigate to it.
     */
    private fun pollForPermissionGranted() {
        val permissionHolder = sectionItem
            ?.filter {
                // Currently, we don't really need to poll for permissions for ItemPurchase.
                // The user will just stay here forever until they manually navigate away.
                !it.isPurchaseItem
            }
            ?.map { sectionItem ->
                sectionItem.media
                    ?.takeIf { media -> media.id == permissionContext.mediaItemId }
                    ?: sectionItem
            }
            ?: pageAndProperty?.map { (_, page) -> page }
            ?: property

        permissionHolder
            .mapNotNull { it.resolvedPermissions?.authorized }
            .distinctUntilChanged()
            .subscribeBy { authorized ->
                if (authorized) {
                    fireEvent(Events.ToastMessage("Permission granted! TODO: impl navigation.."))
                }
            }
            .addTo(disposables)

        propertyStore.fetchPermissionStates(permissionContext.propertyId)
            .doOnSubscribe { Log.i("Starting to fetch permission states.") }
            .doOnComplete { Log.i("Permission states fetched.") }
            .delay(5, TimeUnit.SECONDS)
            .repeat()
            .subscribe()
            .addTo(disposables)
    }
}
