package app.eluvio.wallet.di

import app.eluvio.wallet.data.stores.FabricConfigStore
import app.eluvio.wallet.network.dto.FabricConfiguration
import io.reactivex.rxjava3.core.Single
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The base url we are hitting is dynamic and can change at any time.
 * This means we can't just construct a Retrofit instance at app startup and provide it with Hilt.
 */
@Singleton
class ApiProvider @Inject constructor(
    private val builder: Retrofit.Builder,
    private val configStore: FabricConfigStore,
) {
    private val fabricRetrofitFactory = RetrofitCachedFactory { it.fabricEndpoint }
    private val authdRetrofitFactory = RetrofitCachedFactory { it.authdEndpoint }

    fun <T : Any> getFabricApi(clazz: Class<T>): Single<T> {
        return fabricRetrofitFactory.get().map { it.create(clazz) }
    }

    fun <T : Any> getAuthdApi(clazz: Class<T>): Single<T> {
        return authdRetrofitFactory.get().map { it.create(clazz) }
    }

    // temp place for a way to get the current fabric endpoint
    fun getFabricEndpoint(): Single<String> {
        return fabricRetrofitFactory.get().map { it.baseUrl().toString() }
    }

    private inner class RetrofitCachedFactory(
        private val endpointSelector: (FabricConfiguration) -> String,
    ) {
        // TODO: actually implement failover and don't just cache the first retrofit instance forever.
        private var instance: Retrofit? = null
        fun get(): Single<Retrofit> {
            instance?.let {
                return Single.just(it)
            }
            return configStore.observeFabricConfiguration()
                .firstOrError()
                .map { config ->
                    // TODO impl failover
                    builder.baseUrl("${endpointSelector(config)}/").build()
                }
        }
    }
}

inline fun <reified Api : Any> ApiProvider.getFabricApi(): Single<Api> {
    return getFabricApi(Api::class.java)
}

inline fun <reified Api : Any> ApiProvider.getAuthdApi(): Single<Api> {
    return getAuthdApi(Api::class.java)
}
