package app.eluvio.wallet.data.stores

import app.eluvio.wallet.network.api.FabricConfigApi
import app.eluvio.wallet.network.dto.FabricConfiguration
import app.eluvio.wallet.util.asSharedState
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.timeout
import io.reactivex.rxjava3.core.Flowable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes


@Singleton
class FabricConfigStore @Inject constructor(
    environmentStore: EnvironmentStore,
    private val fabricConfigApi: FabricConfigApi,
) {
    private val configFetchInterval = 3.minutes

    // Effectively an in-memory cache of the config, that will self-refresh as long as someone is subscribed
    // TODO: add persistence?
    private val config: Flowable<FabricConfiguration> =
        environmentStore.observeSelectedEnvironment()
            .switchMapSingle { currentEnv ->
                fabricConfigApi.getConfig(currentEnv.configUrl)
                    .doOnSuccess {
                        Log.d("fetched Config for Env($currentEnv): $it")
                    }
            }
            // As long as someone is subscribed, this will reset every [configFetchInterval]
            .doOnError { Log.e("Error fetching config:", it) }
            .retryWhen {
                // retry with delay on actual errors
                //TODO: Make this retry with backoff
                it.delay(5, TimeUnit.SECONDS)
            }
            .timeout(configFetchInterval)
            // retry immediately on fetch interval "error"
            .retry()
            .asSharedState()

    fun observeFabricConfiguration(): Flowable<FabricConfiguration> = config
}

