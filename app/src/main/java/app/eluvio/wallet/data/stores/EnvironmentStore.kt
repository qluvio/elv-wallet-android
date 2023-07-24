package app.eluvio.wallet.data.stores

import app.eluvio.wallet.data.entities.SelectedEnvEntity
import app.eluvio.wallet.util.realm.asFlowable
import app.eluvio.wallet.util.rx.mapNotNull
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.realm.kotlin.Realm
import io.realm.kotlin.delete
import javax.inject.Inject

class EnvironmentStore @Inject constructor(
    private val realm: Realm
) {
    fun observeSelectedEnvironment(): Flowable<SelectedEnvEntity.Environment> {
        return realm.query(SelectedEnvEntity::class).asFlowable()
            .switchMap {
                if (it.isEmpty()) {
                    // No env set. Default to Main
                    setSelectedEnvironment(SelectedEnvEntity.Environment.Main)
                        .andThen(Flowable.just(it))
                } else {
                    // Do nothing, just emit the current value
                    Flowable.just(it)
                }
            }
            .mapNotNull { it.firstOrNull()?.selectedEnv }
    }

    fun setSelectedEnvironment(environment: SelectedEnvEntity.Environment): Completable {
        return Completable.fromAction {
            realm.writeBlocking {
                delete<SelectedEnvEntity>()
                val entity = SelectedEnvEntity().apply {
                    selectedEnv = environment
                }
                copyToRealm(entity)
            }
        }
    }
}
