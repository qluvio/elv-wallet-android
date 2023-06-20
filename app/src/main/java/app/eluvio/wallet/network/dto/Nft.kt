package app.eluvio.wallet.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NftResponse(
    val contents: List<Nft>,
    val paging: Paging,
)

@JsonClass(generateAdapter = true)
data class Nft(
//    val id: String?,
    val contract_addr: String,
    val token_id: String,
    val block: Int,
//    val created:DateTime,
    val cap: Int,
    val meta: NftMetadata,
    val token_uri: String?,
    val nft_template: NftTemplate,
)

@JsonClass(generateAdapter = true)
data class NftMetadata(
    val description: String?,
    val display_name: String?,
    val edition_name: String?,
    val image: String,
    val playable: Boolean,
)

@JsonClass(generateAdapter = true)
data class NftTemplate(
    val additional_media_custom_css: String?,
//    val additional_media: List<AdditionalMedia>,
    val additional_media_sections: AdditionalMediaSection,
)

@JsonClass(generateAdapter = true)
data class AdditionalMediaSection(
    val featured_media: List<MediaItem>?,
    val sections: List<MediaSection>?,
)

@JsonClass(generateAdapter = true)
data class MediaSection(
    val id: String,
    val name: String,
    val collections: List<MediaCollection>,
)

@JsonClass(generateAdapter = true)
data class MediaCollection(
    val id: String?,
    val name: String?,
    val display: String?,
    val media: List<MediaItem>?,
    val collections: List<MediaCollection>?,
)

@JsonClass(generateAdapter = true)
data class MediaItem(
    val id: String,
    val name: String,
    val display: String?,
//    val media: List<Media>,
)

@JsonClass(generateAdapter = true)
data class MediaLink(
    val sources: MediaSource
)

@JsonClass(generateAdapter = true)
data class MediaSource(
    val default: TheDefaultThing,
)

@JsonClass(generateAdapter = true)
data class TheDefaultThing(
    @field:Json(name = ".") val dot: TheDotThing,
    @field:Json(name = "/") val optionsPath: String,
)

@JsonClass(generateAdapter = true)
data class TheDotThing(
    val container: String,
)

@JsonClass(generateAdapter = false)
enum class MediaType {
    Video,
    Audio,
    Image,
    Gallery,
    HTML,
    Ebook,
    ;
}
