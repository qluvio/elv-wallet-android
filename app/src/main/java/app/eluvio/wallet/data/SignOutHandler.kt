package app.eluvio.wallet.data

import app.eluvio.wallet.data.stores.TokenStore
import app.eluvio.wallet.data.stores.UserStore
import javax.inject.Inject

class SignOutHandler @Inject constructor(
    private val tokenStore: TokenStore,
    private val userStore: UserStore
) {
    fun signOut() {
        tokenStore.wipe()
        // delete entire db?
        userStore.deleteAll()
    }
}
