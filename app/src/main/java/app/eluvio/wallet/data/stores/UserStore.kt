package app.eluvio.wallet.data.stores

import app.eluvio.wallet.data.entities.UserEntity
import app.eluvio.wallet.util.mapNotNull
import app.eluvio.wallet.util.realm.asFlowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.schedulers.Schedulers
import io.realm.kotlin.Realm
import javax.inject.Inject

class UserStore @Inject constructor(
    private val realm: Realm
) {
    fun getCurrentUser(): Maybe<UserEntity> {
        return realm.query(UserEntity::class).asFlowable().firstOrError()
            .mapNotNull { it.firstOrNull() }
    }

    fun saveUser(walletAddress: String) {
        Schedulers.io().scheduleDirect {
            realm.writeBlocking {
                copyToRealm(UserEntity().apply { this.walletAddress = walletAddress })
            }
        }
    }

    fun deleteAll() {
        Schedulers.io().scheduleDirect {
            realm.writeBlocking {
                delete(UserEntity::class)
            }
        }
    }
}
