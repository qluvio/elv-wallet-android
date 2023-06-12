package app.eluvio.wallet.util.sqldelight

import app.eluvio.wallet.sqldelight.User
import app.eluvio.wallet.util.base58

val User.userId: String get() = "iusr${address.base58}"
