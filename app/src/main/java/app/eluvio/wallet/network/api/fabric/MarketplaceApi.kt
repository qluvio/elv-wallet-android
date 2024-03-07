package app.eluvio.wallet.network.api.fabric

import app.eluvio.wallet.network.dto.AssetLinkDto
import app.eluvio.wallet.network.dto.MediaLinkDto
import com.squareup.moshi.JsonClass
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface MarketplaceApi : FabricApi {
    @GET("q/{marketplace}/meta/public/asset_metadata")
    fun getMarketplaceInfo(@Path("marketplace") marketplace: String): Single<MarketplaceDto>
}

@JsonClass(generateAdapter = true)
data class MarketplaceDto(
    val info: MarketplaceInfoDto,
    val tenant_id: String,
)

@JsonClass(generateAdapter = true)
data class MarketplaceInfoDto(val items: List<MarketplaceItemDto>)

@JsonClass(generateAdapter = true)
data class MarketplaceItemDto(val sku: String, val nft_template: KindOfNftTemplate?)

@JsonClass(generateAdapter = true)
data class KindOfNftTemplate(val nft: NftTemplate2?, val title: String?)

@JsonClass(generateAdapter = true)
data class NftTemplate2(
    val description: String,
    val address: String,
    val image: String?,

    val display_name: String?,
    val edition_name: String?,
//    val additional_media_sections: AdditionalMediaSection2?,
)

@JsonClass(generateAdapter = true)
data class AdditionalMediaSection2(
    val featured_media: List<MediaItemDto2>?,
//    val sections: List<MediaSectionDto>?,
)

@JsonClass(generateAdapter = true)
data class MediaItemDto2(
    val id: String,
    val name: String?,
    val display: String?,
    val image: String?,
    val media_type: String?,
    val image_aspect_ratio: String?,
    val poster_image: AssetLinkDto?,
    val media_file: AssetLinkDto?,
    val media_link: MediaLinkDto?,
    val background_image_tv: AssetLinkDto?,
)
