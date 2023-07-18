package app.eluvio.wallet.screens.redeemdialog

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.NftEntity
import app.eluvio.wallet.data.entities.RedeemableOfferEntity
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.data.stores.FulfillmentStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.screens.destinations.RedeemDialogDestination
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.realm.toDate
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.realm.kotlin.types.RealmInstant
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RedeemDialogViewModel @Inject constructor(
    stateHandle: SavedStateHandle,
    private val contentStore: ContentStore,
    private val apiProvider: ApiProvider,
    private val fulfillmentStore: FulfillmentStore,
) : BaseViewModel<RedeemDialogViewModel.State>(State()) {
    data class State(
        val title: String = "",
        val image: String? = null,
        val offerValid: Boolean = false,
        val dateRange: String = "",
        val offerStatus: Status = Status.UNREDEEMED,
        // If offer is redeemed, this is the transaction hash
        val transaction: String? = null,
    ) {
        enum class Status {
            UNREDEEMED, REDEEMED, REDEEMING
        }
    }

    private val contractAddress = RedeemDialogDestination.argsFrom(stateHandle).contractAddress
    private val tokenId = RedeemDialogDestination.argsFrom(stateHandle).tokenId
    private val offerId = RedeemDialogDestination.argsFrom(stateHandle).offerId

    private var refreshDisposable: Disposable? = null
    private var fulfillmentDataDisposable: Disposable? = null

    override fun onResume() {
        super.onResume()
        apiProvider.getFabricEndpoint().flatMapPublisher { endpoint ->
            contentStore.observeNft(contractAddress, tokenId)
                .doOnNext {
                    // Already fetched before we got here, but just in case it failed or got
                    // canceled before finishing, fetch info again.
                    refreshNftInfoOnce(it)
                }
                .map { it to endpoint }
        }
            .map { (nft, endpoint) ->
                val offer = nft.redeemableOffers.firstOrNull { it.offerId == offerId }
                    ?: error("Offer not found")
                val redeemState = nft.redeemStates.firstOrNull { it.offerId == offerId }
                    ?: error("Offer state not found")
                val status = if (redeemState.redeemed == null) {
                    State.Status.UNREDEEMED
                } else {
                    State.Status.REDEEMED
                }
                val transaction = redeemState.transaction
                prefetchFulfillmentData(transaction)
                val image = (offer.posterImagePath ?: offer.imagePath)?.let { "$endpoint$it" }
                State(
                    offer.name,
                    image,
                    offer.isValid,
                    offer.dateRange,
                    offerStatus = status,
                    transaction = transaction
                )
            }
            .subscribeBy(
                onNext = { updateState { it } },
                onError = {}
            )
            .addTo(disposables)

    }

    /**
     * Immediately returns the nft, but won't complete until nft info is fetched from network if nft as redeemable offers.
     */
    private fun refreshNftInfoOnce(nft: NftEntity) {
        if (refreshDisposable == null) {
            refreshDisposable = contentStore.refreshRedeemedOffers(nft)
                .doOnError { Log.w("prefetch offer info failed. This could cause problems in viewing offer $it") }
                .retry(2)
                .subscribeBy(onError = {
                    Log.e("prefetched failed and not retrying!")
                })
                .addTo(disposables)
        }
    }

    private fun prefetchFulfillmentData(transaction: String?) {
        if (fulfillmentDataDisposable == null && transaction != null) {
            fulfillmentDataDisposable =
                fulfillmentStore.prefetchFulfillmentData(transaction)
                    .retry(1)
                    .subscribeBy(onError = {
                        Log.e(
                            "prefetch fulfillment data failed and not retrying! transaction=$transaction",
                            it
                        )
                    })
                    .addTo(disposables)
        }
    }

    private val RedeemableOfferEntity.isValid: Boolean
        get() {
            val availableAt = availableAt ?: return false
            val expiresAt = expiresAt ?: return false
            return RealmInstant.now() in availableAt..expiresAt
        }

    private val RedeemableOfferEntity.dateRange: String
        get() {
            val formatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            val availableAt = availableAt?.toDate() ?: return ""
            val expiresAt = expiresAt?.toDate() ?: return ""
            return "${formatter.format(availableAt)} - ${formatter.format(expiresAt)}"
        }
}
