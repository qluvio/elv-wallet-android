package app.eluvio.wallet.data

import app.eluvio.wallet.data.converters.toEntity
import app.eluvio.wallet.data.entities.VideoOptionsEntity
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.network.api.fabric.AssetFetcherApi
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class VideoOptionsFetcher @Inject constructor(
    private val apiProvider: ApiProvider,
    private val contentStore: ContentStore,
) {
    fun fetchVideoOptions(mediaItemId: String): Single<VideoOptionsEntity> {
        return apiProvider.getApi(AssetFetcherApi::class)
            .flatMap { api -> fetchVideoOptions(api, mediaItemId) }
    }

    private fun fetchVideoOptions(
        api: AssetFetcherApi,
        mediaItemId: String
    ): Single<VideoOptionsEntity> {
        return contentStore.observeMediaItem(mediaItemId)
            .firstOrError()
            .flatMap { mediaItem ->
                // TODO this gets the first "offering" (default?)
                val path = mediaItem.mediaLinks.values.firstOrNull()
                    ?: throw RuntimeException("No media link found for $mediaItemId")
                api.getVideoOptions(path).map { response ->
                    val url = response.raw().request.url.toString()
                    val pathDelimiter = if (url.contains("%2F")) "%2F" else "/"
                    response.body()?.toEntity(url.substringBeforeLast(pathDelimiter))
                        ?: throw RuntimeException("No supported video formats found from $url")
                }
            }
    }
}
