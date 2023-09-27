package app.eluvio.wallet.network.api.fabric

import com.squareup.moshi.JsonClass
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface MarketplaceApi : FabricApi {
    @GET("q/{marketplace}/meta/public/asset_metadata")
    fun getMarketplaceInfo(@Path("marketplace") marketplace: String): Single<MarketplaceDto>
}

@JsonClass(generateAdapter = true)
data class MarketplaceDto(val info: MarketplaceInfoDto)

@JsonClass(generateAdapter = true)
data class MarketplaceInfoDto(val items: List<MarketplaceItemDto>)

@JsonClass(generateAdapter = true)
data class MarketplaceItemDto(val sku: String, val nft_template: KindOfNftTemplate?)

@JsonClass(generateAdapter = true)
data class KindOfNftTemplate(val nft: AvoidTheBadStuffTemplate?)

@JsonClass(generateAdapter = true)
data class AvoidTheBadStuffTemplate(
    val description: String,
    val address: String
)
