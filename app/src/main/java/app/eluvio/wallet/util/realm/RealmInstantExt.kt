package app.eluvio.wallet.util.realm

import io.realm.kotlin.types.RealmInstant
import java.util.Date
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Converts a [RealmInstant] to a [Date].
 */
fun RealmInstant.toDate() =
    Date(
        epochSeconds.seconds.inWholeMilliseconds +
                nanosecondsOfSecond.nanoseconds.inWholeMilliseconds
    )

/**
 * Only accurate to seconds (when minSdk >= 26 we can use LocalDateTime instead).
 */
fun Date.toRealmInstant(): RealmInstant {
    return RealmInstant.from(time / 1000, 0)
}
