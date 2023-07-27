package app.eluvio.wallet.network.adapters

import app.eluvio.wallet.network.dto.AssetLinkDto
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Parses fabric links.
 * Using the json path instead of the literal file url proved to be more difficult than just
 * querying the "." and "/" portions to construct a direct path.
 */
class AssetLinkAdapter {
    @FromJson
    fun fromJson(assetLinkJson: AssetLinkJson): AssetLinkDto {
        return if (assetLinkJson.slash.startsWith("/qfab/")) {
            // When the link contains "qfab", it also has a different "container" value,
            // in that case we ignore [assetLinkJson.dot.container]
            val filePath = assetLinkJson.slash.removePrefix("/qfab/")
            AssetLinkDto("q/$filePath")
        } else {
            val hash = assetLinkJson.dot.container
            val filePath = assetLinkJson.slash.removePrefix("./")
            AssetLinkDto("q/$hash/$filePath")
        }
    }
}

@JsonClass(generateAdapter = true)
data class AssetLinkJson(
    @field:Json(name = ".") val dot: LinkContainer,
    @field:Json(name = "/") val slash: String
)

@JsonClass(generateAdapter = true)
data class LinkContainer(
    @field:Json(name = "container") val container: String
)
