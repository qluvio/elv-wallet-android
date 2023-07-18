package app.eluvio.wallet.data

import app.eluvio.wallet.data.stores.TokenStore
import app.eluvio.wallet.util.logging.Log
import io.reactivex.rxjava3.core.Completable
import io.realm.kotlin.Realm
import javax.inject.Inject

class SignOutHandler @Inject constructor(
    private val tokenStore: TokenStore,
    private val realm: Realm
) {
    fun signOut(): Completable {
        return Completable.fromAction {
            tokenStore.wipe()
            // delete entire db?
            realm.writeBlocking {
                Log.w("Deleting all realm data")
                deleteAll()
            }
        }
    }
}
