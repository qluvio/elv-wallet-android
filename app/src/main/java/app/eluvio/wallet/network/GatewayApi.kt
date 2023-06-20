package app.eluvio.wallet.network

import app.eluvio.wallet.network.dto.NftResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Url

interface GatewayApi {
    @GET
    fun getNfts(@Url url: String): Single<NftResponse>
}
