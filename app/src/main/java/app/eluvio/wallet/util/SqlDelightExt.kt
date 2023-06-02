package app.eluvio.wallet.util

import app.cash.sqldelight.Query
import app.cash.sqldelight.rx3.asObservable
import app.cash.sqldelight.rx3.mapToOneNonNull
import io.reactivex.rxjava3.core.Maybe


fun <T : Any> Query<T>.asMaybe(): Maybe<T> = asObservable().mapToOneNonNull().firstElement()
