package app.eluvio.wallet.data

import app.eluvio.wallet.data.converters.toEntity
import app.eluvio.wallet.data.entities.VideoOptionsEntity
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.network.GatewayApi
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class VideoOptionsFetcher @Inject constructor(
    private val gatewayApi: GatewayApi,
    private val contentStore: ContentStore,
) {
    fun fetchVideoOptions(mediaItemId: String): Single<VideoOptionsEntity> {
        return contentStore.observeMediaItem(mediaItemId)
            .firstOrError()
            .flatMap { mediaItem ->
                val url = mediaItem.mediaLink
                    ?: throw RuntimeException("No media link found for $mediaItemId")
                gatewayApi.getVideoOptions(url).map {
                    it.toEntity(url.substringBeforeLast("/"))
                        ?: throw RuntimeException("No supported video formats found from $url")
                }
            }
    }
}
