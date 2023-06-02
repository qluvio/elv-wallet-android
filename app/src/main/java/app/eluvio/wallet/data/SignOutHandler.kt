package app.eluvio.wallet.data

import javax.inject.Inject

class SignOutHandler @Inject constructor(
    private val tokenStore: TokenStore,
    private val userStore: UserStore
) {
    fun signOut() {
        tokenStore.wipe()
        userStore.deleteAll()
    }
}
