package app.eluvio.wallet.network.converters

import app.eluvio.wallet.data.entities.MediaEntity

object ConverterUtils {
    fun parseAspectRatio(aspectRatioString: String?): Float? {
        return when (aspectRatioString) {
            "Square" -> MediaEntity.ASPECT_RATIO_SQUARE
            "Wide", "Landscape" -> MediaEntity.ASPECT_RATIO_WIDE
            "Poster", "Portrait" -> MediaEntity.ASPECT_RATIO_POSTER
            else -> null
        }
    }
}
