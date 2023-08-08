package app.eluvio.wallet.screens.redeemdialog

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.app.Events
import app.eluvio.wallet.data.entities.NftEntity
import app.eluvio.wallet.data.entities.RedeemStateEntity
import app.eluvio.wallet.data.entities.RedeemableOfferEntity
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.data.stores.FulfillmentStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.destinations.FulfillmentQrDialogDestination
import app.eluvio.wallet.screens.destinations.RedeemDialogDestination
import app.eluvio.wallet.util.crypto.Base58
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.realm.toDate
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.realm.kotlin.types.RealmInstant
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

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
        val offerStatus: RedeemStateEntity.Status = RedeemStateEntity.Status.UNREDEEMED,
        // Only needed by VM, View should not use this
        val _nftEntity: NftEntity? = null,
        // If offer is redeemed, this is the transaction hash
        val _transaction: String? = null,
    )

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
                val transaction = redeemState.transaction
                prefetchFulfillmentData(transaction)
                val image = (offer.posterImagePath ?: offer.imagePath)?.let { "$endpoint$it" }
                State(
                    title = offer.name,
                    image = image,
                    offerValid = offer.isValid,
                    dateRange = offer.dateRange,
                    offerStatus = redeemState.status,
                    _transaction = transaction,
                    _nftEntity = nft,
                )
            }
            .subscribeBy(
                onNext = { updateState { it } },
                onError = {
                    Log.e("Error loading offer ", it)
                }
            )
            .addTo(disposables)

    }

    fun redeemOrShowOffer() {
        state.firstOrError().flatMapCompletable {
            if (it._transaction != null) {
                // offer already redeemed, show fulfillment dialog
                Completable.fromAction {
                    navigateTo(FulfillmentQrDialogDestination(it._transaction).asPush())
                }
            } else {
                redeemOffer(it)
                    .andThen(pollRedemptionStatusUntilComplete(it))
            }
        }
            .subscribeBy(onError = {
                fireEvent(Events.NetworkError)
                Log.e("Error redeeming offer", it)
            })
            .addTo(disposables)
    }

    private fun redeemOffer(state: State): Completable {
        state._nftEntity ?: error("nft is null")
        val reference = Base58.encode(Random.nextBytes(16))
        return fulfillmentStore.initiateRedemption(
            state._nftEntity,
            offerId,
            reference,
        )
    }

    private fun pollRedemptionStatusUntilComplete(state: State): Completable {
        state._nftEntity ?: error("nft is null")
        return Flowable.interval(0, 2, TimeUnit.SECONDS)
            .flatMapSingle {
                fulfillmentStore.refreshRedeemedOffers(state._nftEntity)
            }
            .takeUntil { nft ->
                val status = nft.redeemStates.firstOrNull { it.offerId == offerId }?.status
                status != RedeemStateEntity.Status.REDEEMING
            }
            .ignoreElements()
    }

    /**
     * Immediately returns the nft, but won't complete until nft info is fetched from network if nft as redeemable offers.
     */
    private fun refreshNftInfoOnce(nft: NftEntity) {
        if (refreshDisposable == null) {
            refreshDisposable = fulfillmentStore.refreshRedeemedOffers(nft)
                .ignoreElement()
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
                fulfillmentStore.loadFulfillmentData(transaction)
                    .retry(1)
                    .subscribeBy(
                        onComplete = { Log.d("prefetch complete for $transaction") },
                        onError = {
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
