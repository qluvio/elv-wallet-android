package app.eluvio.wallet.data.stores

import app.eluvio.wallet.data.SignOutHandler
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.NftEntity
import app.eluvio.wallet.data.entities.NftId
import app.eluvio.wallet.data.entities.NftTemplateEntity
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.network.api.authd.GatewayApi
import app.eluvio.wallet.network.converters.toEntity
import app.eluvio.wallet.network.converters.toNfts
import app.eluvio.wallet.network.dto.TokenIdDto
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.realm.asFlowable
import app.eluvio.wallet.util.realm.saveTo
import app.eluvio.wallet.util.rx.mapNotNull
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import javax.inject.Inject


typealias TokenOwnership = TokenIdDto
typealias TemplateOrOwnership = Pair<NftTemplateEntity?, TokenOwnership?>

class ContentStore @Inject constructor(
    private val apiProvider: ApiProvider,
    private val realm: Realm,
    private val signOutHandler: SignOutHandler,
) {

    /** Returns either the NFT template to display, or the owned token for this SKU/Entitlement. */
    fun observeNftBySku(
        marketplace: String,
        sku: String,
        signedEntitlementMessage: String?
    ): Flowable<TemplateOrOwnership> {
        val id = if (signedEntitlementMessage != null) {
            NftId.forEntitlement(signedEntitlementMessage)
        } else {
            NftId.forSku(marketplace, sku)
        }
        val templateFromDb = realm.query<NftTemplateEntity>(
            "${NftTemplateEntity::id.name} == $0",
            id
        )
            .asFlowable()
            .mapNotNull { it.firstOrNull() to null }

        // We could technically start observing the DB before we get token ownership info, but
        // it'll just cause the UI to flicker before navigating away to NftDetails.
        // So instead of we'll only observe the DB if we verified we don't own this SKU/Entitlement yet.
        return fetchTemplateAndOwnership(
            id,
            marketplace,
            sku,
            signedEntitlementMessage
        )
            .map<TemplateOrOwnership> { null to it }
            .toFlowable()
            .switchIfEmpty(templateFromDb)
    }

    /**
     * Fetches the NFT template for a given SKU/Entitlement, and saves it to the database.
     * Template isn't actually returned because it's never used.
     * Returns the owned token id/address if it exists
     */
    private fun fetchTemplateAndOwnership(
        id: String, // When fetched, save nftTemplate under this id.
        marketplace: String,
        sku: String,
        signedEntitlementMessage: String?
    ): Maybe<TokenOwnership> {
        return apiProvider.getApi(GatewayApi::class)
            .flatMap { api -> api.getNftForSku(marketplace, sku, signedEntitlementMessage) }
            .flatMapMaybe { dto ->
                val nftTemplateEntity = dto.nftTemplate.toEntity(id).apply {
                    tenant = dto.tenant
                }
                Single.just(nftTemplateEntity)
                    .saveTo(realm, clearTable = false)
                    .flatMapMaybe {
                        val ownership = dto.entitlementClaimedTokens?.firstOrNull()
                        when {
                            ownership != null -> {
                                Log.d("Server provided entitlement ownership: $ownership")
                                Maybe.just(ownership)
                            }

                            signedEntitlementMessage == null -> {
                                // For SKU without Entitlement, we need to figure out ownership locally
                                Log.d("Starting to manually check ownership for SKU without entitlement.")
                                findOwnedToken(nftTemplateEntity)
                            }

                            else -> {
                                // Server is explicitly telling us that the user doesn't own this entitlement
                                Log.d("No ownership for Entitlement")
                                Maybe.empty()
                            }
                        }
                    }
            }
    }

    /**
     * Finds an owned token for a given SKU. Or an empty Maybe if the user doesn't own the token.
     */
    private fun findOwnedToken(nftTemplateEntity: NftTemplateEntity): Maybe<TokenOwnership> {
        return observeWalletData(forceRefresh = false)
            .mapNotNull { nfts ->
                nfts.getOrNull()?.firstOrNull { nft ->
                    nft.contractAddress == nftTemplateEntity.contractAddress
                }
            }
            .map { nft -> TokenOwnership(nft.contractAddress, nft.tokenId) }
            .firstElement()
    }

    fun observeWalletData(forceRefresh: Boolean = true): Flowable<Result<List<NftEntity>>> {
        return realm.query<NftEntity>()
            .sort(NftEntity::createdAt.name, Sort.DESCENDING)
            .asFlowable()
            .map { Result.success(it) }
            .mergeWith(
                if (forceRefresh) {
                    fetchWalletData()
                        .mapNotNull { nfts ->
                            // TODO: this is a hack to double-emit an empty list when network returns empty so that the viewmodel can stop the loading state.
                            // fetchWalletData() should be a completable that doesn't emit items.
                            Result.success(nfts).takeIf { nfts.isEmpty() }
                        }
                        .onErrorReturn { Result.failure(it) }
                } else {
                    Maybe.empty()
                }
            )
            .doOnNext {
                it.exceptionOrNull()?.let { error ->
                    Log.e("Error in wallet data stream", error)
                }
            }
    }

    fun observeNft(contractAddress: String, tokenId: String): Flowable<NftEntity> {
        val id = NftId.forToken(contractAddress, tokenId)
        return realm.query<NftEntity>("${NftEntity::id.name} == $0", id)
            .asFlowable()
            .switchMapSingle { list ->
                val item = list.firstOrNull()
                if (item == null) {
                    // Missing item in db, maybe we got here from a deeplink?
                    // calling fetchWalletData is overkill, but this is a demo
                    fetchWalletData()
                        .map { nfts ->
                            nfts.firstOrNull { nft ->
                                nft.contractAddress == contractAddress && nft.tokenId == tokenId
                            } ?: throw NftNotFoundException()
                        }
                } else {
                    Single.just(item)
                }
            }
            .distinctUntilChanged()
    }

    fun observeMediaItem(mediaId: String): Flowable<MediaEntity> {
        return realm.query<MediaEntity>(
            "${MediaEntity::id.name} == $0",
            mediaId
        ).asFlowable()
            .mapNotNull { it.firstOrNull() }
    }

    fun fetchWalletData(): Single<List<NftEntity>> {
        return apiProvider.getApi(GatewayApi::class)
            .flatMap { api -> api.getNfts() }
            .map { response -> response.toNfts() }
            .saveTo(realm, clearTable = true)
            .retry { count, error ->
                // There's a rare edge case where the API gateway validates our token, but by the time it hits fabric, the token is expired.
                // In that case it's worth retrying once. To let the auto-refresh mechanisms kick in.
                count == 1 && error is IllegalStateException
            }
            .onErrorResumeNext { error ->
                if (error is IllegalStateException) {
                    // This is a fabric error. Probably Bad/expired token and we need to sign out.
                    signOutHandler.signOut("Token expired. Please sign in again.")
                        // Consume the error. App will restart.
                        .andThen(Single.just(emptyList()))
                } else {
                    Single.error(error)
                }
            }
    }
}

class NftNotFoundException : Exception("Nft not found in wallet data")
