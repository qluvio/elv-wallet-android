package app.eluvio.wallet.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NftResponse(
    val contents: List<NftDto>,
    val paging: Paging,
)

@JsonClass(generateAdapter = true)
data class NftDto(
    val contract_addr: String,
    val token_id: String,
    val block: Int,
    val cap: Int,
    val meta: NftMetadataDto,
    val token_uri: String?,
    val nft_template: NftTemplateDto,
)

@JsonClass(generateAdapter = true)
data class NftMetadataDto(
    val description: String?,
    val display_name: String?,
    val edition_name: String?,
    val image: String,
    val playable: Boolean,
)

@JsonClass(generateAdapter = true)
data class NftTemplateDto(
    val additional_media_sections: AdditionalMediaSectionDto?,
)

@JsonClass(generateAdapter = true)
data class AdditionalMediaSectionDto(
    val featured_media: List<MediaItemDto>?,
    val sections: List<MediaSectionDto>?,
)

@JsonClass(generateAdapter = true)
data class MediaSectionDto(
    val id: String,
    val name: String,
    val collections: List<MediaCollectionDto>,
)

@JsonClass(generateAdapter = true)
data class MediaCollectionDto(
    val id: String?,
    val name: String?,
    val display: String?,
    val media: List<MediaItemDto>?,
)

@JsonClass(generateAdapter = true)
data class MediaItemDto(
    val id: String,
    val name: String,
    val display: String?,
    val image: String?,
    val media_type: String?,
    val media_file: AssetLinkDto?,
    val media_link: MediaLinkDto?,
    val background_image_tv: AssetLinkDto?,
    val gallery: List<GalleryItemDto>?,
)

@JsonClass(generateAdapter = true)
data class MediaLinkDto(
    val sources: Map<String, AssetLinkDto>?
)

@JsonClass(generateAdapter = true)
data class GalleryItemDto(
    val name: String,
    val image: AssetLinkDto?,
)
