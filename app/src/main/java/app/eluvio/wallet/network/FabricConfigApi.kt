package app.eluvio.wallet.network

import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Url

interface FabricConfigApi {
    @GET
    fun getConfig(@Url configUrl: String): Single<FabricConfiguration>
}
