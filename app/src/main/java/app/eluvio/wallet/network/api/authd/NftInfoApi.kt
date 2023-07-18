package app.eluvio.wallet.network.api.authd

import app.eluvio.wallet.network.dto.FulfillmentResponseDto
import app.eluvio.wallet.network.dto.NftInfoDto
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface NftInfoApi : AuthdApi {
    @GET("nft/info/{contractAddress}/{tokenId}")
    fun getNftInfo(
        @Path("contractAddress") contractAddress: String,
        @Path("tokenId") tokenId: String
    ): Single<NftInfoDto>

    @GET("https://appsvc.svc.eluv.io/code-fulfillment/{network}/fulfill/{tx_hash}")
    fun getFulfillmentData(
        @Path("network") networkName: String,
        @Path("tx_hash") transactionHash: String
    ): Single<FulfillmentResponseDto>
}
