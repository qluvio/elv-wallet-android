package app.eluvio.wallet.data

import app.eluvio.wallet.sqldelight.User
import app.eluvio.wallet.sqldelight.UserQueries
import app.eluvio.wallet.util.asMaybe
import io.reactivex.rxjava3.core.Maybe
import javax.inject.Inject

class UserStore @Inject constructor(
    private val userQueries: UserQueries
) {
    fun getCurrentUser(): Maybe<User> = userQueries.getCurrentUser().asMaybe()

    fun saveUser(walletAddress: String) {
        userQueries.insert(walletAddress)
    }
}
