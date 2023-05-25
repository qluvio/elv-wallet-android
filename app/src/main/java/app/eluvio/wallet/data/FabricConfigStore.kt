package app.eluvio.wallet.data

import app.eluvio.wallet.network.FabricConfigApi
import app.eluvio.wallet.network.FabricConfiguration
import app.eluvio.wallet.util.asSharedState
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.timeout
import io.reactivex.rxjava3.core.Observable
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
    private val config: Observable<FabricConfiguration> =
        environmentStore.observeSelectedEnvironment()
            .switchMapSingle { currentEnv ->
                fabricConfigApi.getConfig(currentEnv.configUrl)
                    .doOnSuccess {
                        Log.d("fetched Config for Env($currentEnv): $it")
                    }
            }
            // As long as someone is subscribed, this will reset every [configFetchInterval]
            .timeout(configFetchInterval)
            .retry()
            .asSharedState()

    fun observeFabricConfiguration(): Observable<FabricConfiguration> = config
}

