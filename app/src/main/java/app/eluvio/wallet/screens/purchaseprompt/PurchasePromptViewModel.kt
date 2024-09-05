package app.eluvio.wallet.screens.purchaseprompt

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.v2.permissions.PermissionContext
import app.eluvio.wallet.data.entities.v2.permissions.PermissionSettings
import app.eluvio.wallet.data.stores.Environment
import app.eluvio.wallet.data.stores.EnvironmentStore
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.navigation.asReplace
import app.eluvio.wallet.navigation.onClickDirection
import app.eluvio.wallet.screens.common.generateQrCode
import app.eluvio.wallet.screens.destinations.PropertyDetailDestination
import app.eluvio.wallet.screens.navArgs
import app.eluvio.wallet.util.crypto.Base58
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.rx.Optional
import app.eluvio.wallet.util.rx.asSharedState
import app.eluvio.wallet.util.rx.mapNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.json.JSONArray
import org.json.JSONObject
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
        val permissionSettings = sectionItem?.map {
            // Will be empty for non-media section items. For now that's fine.
            Optional.of(it.media?.resolvedPermissions)
        }
            ?: pageAndProperty?.map { (_, page) -> Optional.of(page.pagePermissions) }
            ?: property.map { Optional.of(it.propertyPermissions) }

        environmentStore.observeSelectedEnvironment()
            .firstOrError(/*Assume env won't change while in this screen*/)
            .flatMapPublisher { env ->
                permissionSettings.flatMapSingle { permissionSettings ->
                    generateQrCode(
                        buildPurchaseUrl(permissionContext, env, permissionSettings.orDefault(null))
                    )
                }
            }
            .subscribeBy(
                onNext = { updateState { copy(qrImage = it) } },
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
        // Depending on the context, emit a nav event for the next screen when the user gains
        // access to the relevant resource.
        val navEvent = sectionItem
            ?.mapNotNull { sectionItem ->
                // The only section items that can lead us here are ItemPurchase and Media.
                // We currently don't handle ItemPurchase auto-forward, so we can just handle Media.
                // However, this is brittle code, as anything we direct here in the future will
                // probably just hang forever
                sectionItem.media
                    ?.takeIf { media -> media.resolvedPermissions?.authorized == true }
                    ?.onClickDirection(permissionContext)?.asReplace()
            }
            ?: pageAndProperty
                ?.filter { (_, page) -> page.pagePermissions?.authorized == true }
                ?.map { (property, page) ->
                    PropertyDetailDestination(property.id, page.id).asReplace()
                }
            ?: property
                .filter { it.propertyPermissions?.authorized == true }
                .map { PropertyDetailDestination(it.id).asReplace() }

        navEvent.firstOrError()
            .subscribeBy { navigationEvent ->
                navigateTo(navigationEvent)
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

/**
 * Creates a Wallet URL for either purchasing a specific item, or being offered multiple items that
 * will unlock access for this [PermissionContext].
 */
private fun buildPurchaseUrl(
    permissionContext: PermissionContext,
    environment: Environment,
    permissionSettings: PermissionSettings?,
): String {
    val context = JSONObject()
        // Only supported type is "purchase" for now.
        .put("type", "purchase")
        // Start from the most broad id and keep overriding with more specific ids.
        // Any field that is [null] will not override the previous value.
        .putOpt("id", permissionContext.propertyId)
        .putOpt("id", permissionContext.pageId)
        .putOpt("id", permissionContext.sectionId)
        .putOpt("id", permissionContext.sectionItemId)
        .putOpt("id", permissionContext.mediaItemId)
        // Everything else we have explicitly specified.
        .putOpt("sectionSlugOrId", permissionContext.sectionId)
        .putOpt("sectionItemId", permissionContext.sectionItemId)
        .putOpt(
            "permissionItemIds",
            permissionSettings?.permissionItemIds
                ?.takeIf { it.isNotEmpty() }
                ?.let { JSONArray(it) }
        )
        .putOpt("secondaryPurchaseOption", permissionSettings?.secondaryMarketPurchaseOption)

    val encodedContext = Base58.encode(context.toString().toByteArray())
    return "${environment.walletUrl}/${permissionContext.propertyId}/${permissionContext.pageId.orEmpty()}?p=$encodedContext"
}
