package app.eluvio.wallet.network.api.authd

import app.eluvio.wallet.network.dto.NftResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET

interface GatewayApi : AuthdApi {
    @GET("apigw/nfts")
    fun getNfts(): Single<NftResponse>
}
