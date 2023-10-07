package app.eluvio.wallet.data.stores

import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.network.api.authd.NftClaimApi
import app.eluvio.wallet.network.dto.InitiateNftClaimRequest
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

    /**
     * Claim an NFT for the given tenant/marketplace/sku.
     * Returns the "operation" key to use for checking the status of the claim.
     */
    fun initiateNftClaim(
        tenant: String, marketplaceId: String, sku: String
    ): Single<String> {
        val request = InitiateNftClaimRequest(marketplaceId, sku)
        return apiProvider.getApi(NftClaimApi::class)
            .flatMap { api -> api.claimNft(tenant, request) }
            .map { it.operationKey }
    }

    /**
     * @param op The operation key returned by [initiateNftClaim]
     */
    fun checkNftClaimStatus(tenant: String, op: String): Single<NftClaimResult> {
        return apiProvider.getApi(NftClaimApi::class)
            .flatMap { api -> api.getClaimStatus(tenant) }
            .map { dto ->
                dto
                    .firstOrNull { it.operationKey == op }
                    ?.extra?.claimResult?.let { result ->
                        NftClaimResult.Success(result.tokenId, result.contractAddress)
                    }
                    ?: NftClaimResult.Pending
            }
    }
}
