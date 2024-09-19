package app.eluvio.wallet.data

import androidx.media3.exoplayer.source.MediaSource
import app.eluvio.wallet.data.entities.VideoOptionsEntity
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.data.stores.TokenStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.network.api.authd.VideoPlayoutApi
import app.eluvio.wallet.network.api.fabric.AssetFetcherApi
import app.eluvio.wallet.network.converters.toEntity
import app.eluvio.wallet.screens.videoplayer.VideoOptionsConverter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.zipWith
import javax.inject.Inject

class VideoOptionsFetcher @Inject constructor(
    private val apiProvider: ApiProvider,
    private val contentStore: ContentStore,
    private val tokenStore: TokenStore,
    private val videoOptionsConverter: VideoOptionsConverter,
) {
    fun fetchVideoOptions(mediaItemId: String, propertyId: String?): Single<VideoOptionsEntity> {
        return if (propertyId != null) {
            fetchVideoOptionsFromProperty(propertyId, mediaItemId)
        } else {
            contentStore.observeMediaItem(mediaItemId)
                .firstOrError()
                .flatMap { mediaItem ->
                    val hash = mediaItem.playableHash
                    if (hash != null) {
                        fetchVideoOptionsFromHash(hash)
                    } else {
                        val path = mediaItem.mediaLinks.values.firstOrNull()
                            ?: throw RuntimeException("No media link found for $mediaItemId")
                        fetchVideoOptionsFromPath(path)
                    }
                }
        }
    }

    /**
     * This is the preferred way to fetch video options. It's the only way the backend sets
     * start/end fields correctly for clip playback
     */
    private fun fetchVideoOptionsFromProperty(
        propertyId: String,
        mediaItemId: String
    ): Single<VideoOptionsEntity> {
        return apiProvider.getApi(VideoPlayoutApi::class)
            .zipWith(apiProvider.getFabricEndpoint())
            .flatMap { (api, baseUrl) ->
                api.getVideoOptions(
                    propertyId = propertyId,
                    mediaItemId = mediaItemId
                ).map { response ->
                    response.toEntity(baseUrl, tokenStore.fabricToken.get())
                        ?: throw RuntimeException("No supported video formats found in $response")
                }
            }
    }

    // Calls an API that fetches video options for the latest available hash, instead of directly
    // going to the fabric with the hash we have.
    private fun fetchVideoOptionsFromHash(hash: String): Single<VideoOptionsEntity> {
        return apiProvider.getApi(VideoPlayoutApi::class)
            .zipWith(apiProvider.getFabricEndpoint())
            .flatMap { (api, baseUrl) ->
                api.getVideoOptions(hash).map { response ->
                    response.toEntity(baseUrl, tokenStore.fabricToken.get())
                        ?: throw RuntimeException("No supported video formats found in $response")
                }
            }
    }

    fun fetchVideoOptionsFromPath(fabricPath: String): Single<VideoOptionsEntity> {
        return apiProvider.getApi(AssetFetcherApi::class)
            .flatMap {
                it.getVideoOptions(fabricPath).map { response ->
                    val url = response.raw().request.url.toString()
                    val pathDelimiter = if (url.contains("%2F")) "%2F" else "/"
                    val baseUrl = url.substringBeforeLast(pathDelimiter)
                    response.body()
                        ?.toEntity(baseUrl, tokenStore.fabricToken.get())
                        ?: throw RuntimeException("No supported video formats found from $url")
                }
            }
    }

    fun fetchMediaSourceFromPath(fabricPath: String): Single<MediaSource> {
        return fetchVideoOptionsFromPath(fabricPath)
            .flatMap { videoOptions ->
                videoOptionsConverter.makeMediaSource(videoOptions)
            }
    }
}
