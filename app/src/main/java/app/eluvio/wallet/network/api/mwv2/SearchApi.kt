package app.eluvio.wallet.network.api.mwv2

import app.eluvio.wallet.network.api.authd.AuthdApi
import app.eluvio.wallet.network.dto.PagedContent
import app.eluvio.wallet.network.dto.v2.SearchFiltersDto
import app.eluvio.wallet.network.dto.v2.SearchResultsDto
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SearchApi : AuthdApi {
    @GET("mw/properties/{propertyId}/get_filters")
    fun getSearchFilters(@Path("propertyId") propertyId: String): Single<SearchFiltersDto>

    @GET("ms/properties/{propertyId}/search")
    fun search(
        @Path("propertyId") propertyId: String,
        @Query("limit") limit: Int = 30
    ): Single<PagedContent<SearchResultsDto>>
}
