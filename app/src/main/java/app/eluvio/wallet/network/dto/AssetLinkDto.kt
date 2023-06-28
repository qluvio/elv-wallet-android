package app.eluvio.wallet.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AssetLinkDto(
    /**
     * The full path to the asset. Can be appended on any node endpoint to get the asset.
     */
    val path: String
)
