package app.eluvio.wallet.di

import app.eluvio.wallet.network.dto.AssetLinkDto
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader

/**
 * Parses fabric links.
 * Using the json path instead of the literal file url proved to be more difficult than just
 * querying the "." and "/" portions to construct a direct path.
 */
class AssetLinkAdapter {
    @FromJson
    fun fromJson(jsonReader: JsonReader): AssetLinkDto {
        jsonReader.beginObject()
        var hash: String? = null
        var path: String? = null
        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "." -> hash = getHash(jsonReader)
                "/" -> path = jsonReader.nextString().removePrefix(".")
                else -> jsonReader.skipValue()
            }
        }
        jsonReader.endObject()

        checkNotNull(hash)
        checkNotNull(path)

        return AssetLinkDto("q/$hash/$path")
    }

    private fun getHash(jsonReader: JsonReader): String? {
        try {
            jsonReader.beginObject()
            while (jsonReader.hasNext()) {
                if (jsonReader.nextName() == "container") {
                    return jsonReader.nextString()
                } else {
                    jsonReader.skipValue()
                }
            }
        } finally {
            jsonReader.endObject()
        }
        return null
    }
}
