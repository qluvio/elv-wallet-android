package app.eluvio.wallet.data.stores

import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.network.api.authd.NftClaimApi
import app.eluvio.wallet.network.dto.InitiateNftClaimRequest
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class NftClaimStore @Inject constructor(
    private val apiProvider: ApiProvider
) {
    sealed interface NftClaimResult {
        object Pending : NftClaimResult
        data class Success(val contractAddress: String, val tokenId: String) : NftClaimResult
        //TODO: add error case?
    }

    fun initiateNftClaim(
        tenant: String, marketplaceId: String, sku: String
    ): Completable {
        val request = InitiateNftClaimRequest(marketplaceId, sku)
        return apiProvider.getApi(NftClaimApi::class)
            .flatMapCompletable { api -> api.claimNft(tenant, request) }
    }

    fun checkNftClaimStatus(
        tenant: String,
        marketplaceId: String,
        sku: String
    ): Single<NftClaimResult> {
        return apiProvider.getApi(NftClaimApi::class)
            .flatMap { api -> api.getClaimStatus(tenant) }
            .map { dto ->
                val status = dto
                    .firstOrNull { it.marketplaceId == marketplaceId && it.sku == sku }

                status?.extra?.claimResult?.let { result ->
                    NftClaimResult.Success(result.tokenId, result.contractAddress)
                } ?: NftClaimResult.Pending
            }
    }
}
