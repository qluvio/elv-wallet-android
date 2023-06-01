package app.eluvio.wallet.util

import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

/**
 *
 */
fun <T : Any> Observable<T>.asSharedState() = replay(1).refCount().distinctUntilChanged()

/**
 * Returns an Observable that applies a specified function to each item emitted by the source
 * ObservableSource and emits the results of these function applications,
 * for every result that isn't [null]. [null]s will be filtered out.
 *
 * Makes RxJava and Kotlin play nice so type becomes non-nullable (Any? -> Any)
 * and only emits when it succeeds
 */
fun <T : Any, R : Any> Observable<T>.mapNotNull(mapper: (T) -> R?): Observable<R> = flatMap {
    val result = mapper(it)
    if (result != null) {
        Observable.just(result)
    } else {
        Observable.empty()
    }
}

/**
 * Returns an Observable that applies a specified function to each item emitted by the source
 * ObservableSource and emits the results of these function applications,
 * for every result that isn't [null]. [null]s will be filtered out.
 *
 * Makes RxJava and Kotlin play nice so type becomes non-nullable (Any? -> Any)
 * and only emits when it succeeds
 */
fun <T : Any, R : Any> Single<T>.mapNotNull(mapper: (T) -> R?): Maybe<R> = flatMapMaybe {
    val result = mapper(it)
    if (result != null) {
        Maybe.just(result)
    } else {
        Maybe.empty()
    }
}

fun <T : Any> Observable<T>.timeout(duration: Duration) =
    timeout(duration.inWholeMilliseconds, TimeUnit.MILLISECONDS)
