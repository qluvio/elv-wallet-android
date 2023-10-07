package app.eluvio.wallet.network.dto

import app.eluvio.wallet.network.api.authd.NftClaimApi
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InitiateNftClaimRequest(
    @field:Json(name = "sid") val marketplaceId: String,
    val sku: String,
    val op: String = NftClaimApi.NFT_CLAIM_OPERATION,
)

@JsonClass(generateAdapter = true)
data class NftClaimStatusDto(
    // A composite field of the form: op:marketplaceId:sku:counter
    @field:Json(name = "op") val operationKey: String,
    val status: String?,
    val extra: NftClaimStatusExtraDto?
)

@JsonClass(generateAdapter = true)
data class NftClaimStatusExtraDto(
    @field:Json(name = "0") val claimResult: ClaimResultDto
)

@JsonClass(generateAdapter = true)
data class ClaimResultDto(
    @field:Json(name = "token_addr") val contractAddress: String,
    @field:Json(name = "token_id_str") val tokenId: String,
)
