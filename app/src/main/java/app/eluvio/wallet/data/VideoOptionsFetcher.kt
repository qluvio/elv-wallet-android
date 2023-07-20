package app.eluvio.wallet.data

import app.eluvio.wallet.data.entities.VideoOptionsEntity
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.data.stores.TokenStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.network.api.fabric.AssetFetcherApi
import app.eluvio.wallet.network.converters.toEntity
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class VideoOptionsFetcher @Inject constructor(
    private val apiProvider: ApiProvider,
    private val contentStore: ContentStore,
    private val tokenStore: TokenStore,
) {
    fun fetchVideoOptions(mediaItemId: String): Single<VideoOptionsEntity> {
        return contentStore.observeMediaItem(mediaItemId)
            .firstOrError()
            .flatMap { mediaItem ->
                val path = mediaItem.mediaLinks.values.firstOrNull()
                    ?: throw RuntimeException("No media link found for $mediaItemId")
                fetchVideoOptionsFromPath(path)
            }
    }

    fun fetchVideoOptionsFromPath(path: String): Single<VideoOptionsEntity> {
        return apiProvider.getApi(AssetFetcherApi::class)
            .flatMap {
                it.getVideoOptions(path).map { response ->
                    val url = response.raw().request.url.toString()
                    val pathDelimiter = if (url.contains("%2F")) "%2F" else "/"
                    val baseUrl = url.substringBeforeLast(pathDelimiter)
                    response.body()
                        ?.toEntity(baseUrl, tokenStore.fabricToken)
                        ?: throw RuntimeException("No supported video formats found from $url")
                }
            }
    }
}
