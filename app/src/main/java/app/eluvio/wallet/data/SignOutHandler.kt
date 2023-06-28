package app.eluvio.wallet.data

import app.eluvio.wallet.data.stores.TokenStore
import io.realm.kotlin.Realm
import javax.inject.Inject

class SignOutHandler @Inject constructor(
    private val tokenStore: TokenStore,
    private val realm: Realm
) {
    fun signOut() {
        tokenStore.wipe()
        // delete entire db?
        realm.writeBlocking {
            deleteAll()
        }
    }
}
