package app.eluvio.wallet.util

import app.eluvio.wallet.sqldelight.User

val User.userId: String get() = "iusr${address.base58}"
