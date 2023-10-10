package app.eluvio.wallet.network.api.authd

import app.eluvio.wallet.network.dto.NftForSkuResponse
import app.eluvio.wallet.network.dto.NftResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface GatewayApi : AuthdApi {
    @GET("apigw/nfts")
    fun getNfts(): Single<NftResponse>

    @GET("apigw/marketplaces/{marketplaceId}/sku/{sku}")
    fun getNftForSku(
        @Path("marketplaceId") marketplaceId: String,
        @Path("sku") sku: String
    ): Single<NftForSkuResponse>
}
