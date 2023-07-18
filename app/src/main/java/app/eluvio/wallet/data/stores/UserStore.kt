package app.eluvio.wallet.data.stores

import app.eluvio.wallet.data.entities.UserEntity
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.mapNotNull
import app.eluvio.wallet.util.realm.asFlowable
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import javax.inject.Inject

class UserStore @Inject constructor(
    private val realm: Realm
) {
    fun getCurrentUser(): Maybe<UserEntity> {
        return realm.query<UserEntity>().asFlowable().firstOrError()
            .mapNotNull { it.firstOrNull() }
    }

    fun saveUser(walletAddress: String): Completable {
        return Completable.fromAction {
            Log.d("Saving user: $walletAddress")
            realm.writeBlocking {
                copyToRealm(UserEntity().apply { this.walletAddress = walletAddress })
            }
        }
    }
}
