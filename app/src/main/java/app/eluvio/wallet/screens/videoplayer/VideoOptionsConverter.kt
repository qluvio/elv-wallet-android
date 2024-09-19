package app.eluvio.wallet.screens.videoplayer

import android.annotation.SuppressLint
import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManagerProvider
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import app.eluvio.wallet.data.entities.VideoOptionsEntity
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.internal.ThreadUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private fun getCacheDir(context: Context): File {
    val base = context.getExternalFilesDir(/* type= */ null) ?: context.filesDir
    return File(base, "exo_cache")
}

@SuppressLint("UnsafeOptInUsageError")
@Singleton
class VideoOptionsConverter @Inject constructor(@ApplicationContext context: Context) {
    // Made lazy to ensure init off of ui thread
    private val cache by lazy {
        if (ThreadUtil.isMainThread()) {
            Log.e("stav: wtf dude, we talked about this")
        }
        SimpleCache(
            getCacheDir(context),
            LeastRecentlyUsedCacheEvictor(50 * 1024 * 1024),
            StandaloneDatabaseProvider(context)
        )
    }

    fun makeMediaSource(options: VideoOptionsEntity): Single<MediaSource> {
        return Single.fromCallable { cache }
            .subscribeOn(Schedulers.io())
            .map { cache ->
                options.toMediaSource(cache)
            }
    }
}

@SuppressLint("UnsafeOptInUsageError")
fun VideoOptionsEntity.toMediaSource(cache: Cache? = null): MediaSource {
    // Swap out with OkHttpDataSource?
    val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setDefaultRequestProperties(tokenHeader)
    val dataSourceFactory = if (cache == null) {
        httpDataSourceFactory
    } else {
        CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
    }
//    val dataSourceFactory = DefaultHttpDataSource.Factory()
//        .setDefaultRequestProperties(tokenHeader)
    val drmBuilder = MediaItem.DrmConfiguration.Builder(C.UUID_NIL)
        .setLicenseRequestHeaders(tokenHeader)
        .setMultiSession(true)
    when (drm) {
        VideoOptionsEntity.DRM_WIDEVINE -> {
            drmBuilder.setScheme(C.WIDEVINE_UUID)
                .setLicenseUri(licenseUri)
        }

        VideoOptionsEntity.DRM_CLEAR -> {
            drmBuilder.setScheme(C.CLEARKEY_UUID)
        }

        else -> throw RuntimeException("Unsupported DRM type $drm")
    }
    val mediaItem = makeMediaItem(uri, drmBuilder.build())
    val mediaSourceFactory = when (protocol) {
        VideoOptionsEntity.PROTOCOL_DASH -> DashMediaSource.Factory(dataSourceFactory)
        VideoOptionsEntity.PROTOCOL_HLS -> HlsMediaSource.Factory(dataSourceFactory)
        else -> DefaultMediaSourceFactory(dataSourceFactory)
    }
    Log.i("loading ${protocol}-${drm}")
    return mediaSourceFactory
        .setDrmSessionManagerProvider(DefaultDrmSessionManagerProvider())
        .createMediaSource(mediaItem)
}

private fun makeMediaItem(
    uri: String,
    drmConfiguration: MediaItem.DrmConfiguration
): MediaItem {
    return MediaItem.Builder()
        .setMediaId(uri.substringAfterLast("/"))
        .setUri(uri)
        .setDrmConfiguration(drmConfiguration)
        .build()
}
