package app.eluvio.wallet.screens.purchaseprompt

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.FabricUrl
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.v2.display.DisplaySettings
import app.eluvio.wallet.data.entities.v2.permissions.PermissionSettings
import app.eluvio.wallet.data.permissions.PermissionContext
import app.eluvio.wallet.data.permissions.PermissionContextResolver
import app.eluvio.wallet.data.stores.Environment
import app.eluvio.wallet.data.stores.EnvironmentStore
import app.eluvio.wallet.data.stores.MediaPropertyStore
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
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class PurchasePromptViewModel @Inject constructor(
    private val propertyStore: MediaPropertyStore,
    permissionContextResolver: PermissionContextResolver,
    private val environmentStore: EnvironmentStore,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<PurchasePromptViewModel.State>(State(), savedStateHandle) {

    @Immutable
    data class State(
        val media: MediaEntity? = null,
        val itemPurchase: ItemPurchase? = null,
        val qrImage: Bitmap? = null,
        val bgImageUrl: FabricUrl? = null
    ) {
        @Immutable
        data class ItemPurchase(val displaySettings: DisplaySettings?)
    }

    private val permissionContext = savedStateHandle.navArgs<PermissionContext>()

    private val resolvedContext = permissionContextResolver.resolve(permissionContext)
        .asSharedState()

    override fun onResume() {
        super.onResume()

        updateQrCode()

        updateBackgroundImage()

        updateCardItem()

        pollForPermissionGranted()
    }

    private fun updateQrCode() {
        val permissionSettings = resolvedContext
            .map {
                Optional.of(
                    it.mediaItem?.resolvedPermissions
                        ?: it.sectionItem?.resolvedPermissions
                        ?: it.section?.resolvedPermissions
                        ?: it.page?.pagePermissions
                        ?: it.property.propertyPermissions
                )
            }
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

    private fun updateBackgroundImage() {
        resolvedContext.mapNotNull { context ->
            // If no "page" is defined, default to the property's main page.
            val page = context.page ?: context.property.mainPage
            context.property.loginInfo?.backgroundImageUrl
                ?: page?.backgroundImageUrl
        }
            .subscribeBy(
                onNext = { updateState { copy(bgImageUrl = it) } },
                onError = { Log.e("Error loading background image", it) },
            )
            .addTo(disposables)
    }

    private fun updateCardItem() {
        resolvedContext
            .subscribeBy(
                onNext = { resolved ->
                    val itemPurchase = resolved.sectionItem?.let {
                        State.ItemPurchase(it.displaySettings)
                    }
                    updateState { copy(media = resolved.mediaItem, itemPurchase = itemPurchase) }
                },
                onError = { Log.e("Error loading purchase item", it) }
            )
            .addTo(disposables)
    }

    /**
     * Keep checking permissions to see if the user is now allowed to view the content,
     * then navigate to it.
     */
    private fun pollForPermissionGranted() {
        resolvedContext
            .mapNotNull { resolved ->
                // Depending on the context, emit a nav event for the next screen when the user gains
                // access to the relevant resource.
                when {
                    resolved.mediaItem != null -> {
                        resolved.mediaItem.resolvedPermissions
                            ?.takeIf { it.authorized == true }
                            ?.let { resolved.mediaItem.onClickDirection(permissionContext) }
                    }

                    resolved.section != null || resolved.sectionItem != null -> {
                        // The only SectionItem type we handle auto-forward for is Media.
                        // Known unhandled types: ItemPurchase.
                        null
                    }

                    resolved.page != null -> {
                        resolved.page.pagePermissions
                            ?.takeIf { it.authorized == true }
                            ?.let {
                                PropertyDetailDestination(resolved.property.id, resolved.page.id)
                            }
                    }

                    else -> {
                        resolved.property.propertyPermissions
                            ?.takeIf { it.authorized == true }
                            ?.let { PropertyDetailDestination(resolved.property.id) }
                    }
                }
            }
            .firstOrError(
                // Once permission is granted, stop observing to prevent double-navigation.
            )
            .subscribeBy { destination ->
                navigateTo(destination.asReplace())
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
