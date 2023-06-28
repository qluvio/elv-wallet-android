package app.eluvio.wallet.di

import app.eluvio.wallet.data.stores.FabricConfigStore
import io.reactivex.rxjava3.core.Single
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * The base url we are hitting is dynamic and can change at any time.
 * This means we can't just construct a Retrofit instance at app startup and provide it with Hilt.
 */
class ApiProvider @Inject constructor(
    private val builder: Retrofit.Builder,
    private val configStore: FabricConfigStore,
) {
    // TODO: actually implement failover and don't just cache the first retrofit instance forever.
    private var retrofit: Retrofit? = null

    fun <T : Any> getApi(clazz: Class<T>): Single<T> {
        retrofit?.let {
            return Single.just(it.create(clazz))
        }
        return configStore.observeFabricConfiguration()
            .firstOrError()
            .map { config ->
                // TODO impl failover
                builder.baseUrl("${config.endpoint}/").build()
                    .also { retrofit = it }
                    .create(clazz)
            }
    }
}

inline fun <reified Api : Any> ApiProvider.getApi(): Single<Api> {
    return getApi(Api::class.java)
}
