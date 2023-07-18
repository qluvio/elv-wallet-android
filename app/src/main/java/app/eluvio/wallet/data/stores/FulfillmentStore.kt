package app.eluvio.wallet.data.stores

import app.eluvio.wallet.data.entities.FulfillmentDataEntity
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.network.api.authd.NftInfoApi
import app.eluvio.wallet.network.converters.toEntity
import app.eluvio.wallet.util.mapNotNull
import app.eluvio.wallet.util.realm.asFlowable
import app.eluvio.wallet.util.realm.saveTo
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.zipWith
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import javax.inject.Inject

class FulfillmentStore @Inject constructor(
    private val apiProvider: ApiProvider,
    private val envStore: EnvironmentStore,
    private val realm: Realm,
) {
    fun prefetchFulfillmentData(transactionHash: String): Completable {
        return apiProvider.getApi(NftInfoApi::class)
            .zipWith(envStore.observeSelectedEnvironment().firstOrError())
            .flatMap { (api, config) ->
                api.getFulfillmentData(config.properEnvName, transactionHash)
            }
            .map { it.toEntity(transactionHash) }
            .saveTo(realm)
            .ignoreElement()
    }

    fun observeFulfillmentData(transactionHash: String) =
        realm.query<FulfillmentDataEntity>(
            "${FulfillmentDataEntity::transactionHash.name} = $0",
            transactionHash
        )
            .asFlowable()
            .mapNotNull { it.firstOrNull() }
}
