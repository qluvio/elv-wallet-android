package app.eluvio.wallet.app

import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.size.pxOrElse
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * Coil interceptor that adds width and height query parameters to contentfabric.io image urls.
 * This can save megabytes of bandwidth per image.
 * Inspiration: https://github.com/android/compose-samples/blob/main/Crane/app/src/main/java/androidx/compose/samples/crane/util/UnsplashSizingInterceptor.kt
 */
class ContentFabricSizingInterceptor : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val data = chain.request.data
        val widthPx = chain.size.width.pxOrElse { -1 }
        val heightPx = chain.size.height.pxOrElse { -1 }
        if (widthPx > 0 && heightPx > 0 &&
            data is String && data.contains("contentfabric.io")
        ) {
            val url = data.toHttpUrl()
                .newBuilder()
                .addQueryParameter("width", widthPx.toString())
                .addQueryParameter("height", heightPx.toString())
                .build()
            val request = chain.request.newBuilder().data(url).build()
            return chain.proceed(request)
        }
        return chain.proceed(chain.request)
    }
}
