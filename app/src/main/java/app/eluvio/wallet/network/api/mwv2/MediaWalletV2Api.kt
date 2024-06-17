package app.eluvio.wallet.network.api.mwv2

import app.eluvio.wallet.network.api.authd.AuthdApi
import app.eluvio.wallet.network.dto.PagedContent
import app.eluvio.wallet.network.dto.v2.MediaPageSectionDto
import app.eluvio.wallet.network.dto.v2.MediaPropertyDto
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MediaWalletV2Api : AuthdApi {
    /**
     * Get a list of all properties we have access to.
     */
    @GET("mw/properties")
    fun getProperties(): Single<PagedContent<MediaPropertyDto>>

    /**
     * Request a list of sections by their IDs.
     */
    @POST("mw/properties/{propertyId}/sections")
    fun getSectionsById(
        @Path("propertyId") propertyId: String,
        @Body request: List<String>
    ): Single<PagedContent<MediaPageSectionDto>>
}
