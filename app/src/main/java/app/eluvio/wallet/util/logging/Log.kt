@file:Suppress("NOTHING_TO_INLINE")
package app.eluvio.wallet.util.logging

import timber.log.Timber

// Functions are inlined so that Timber sets the tag to the actual calling class.
// Without the inline, the tag is always "Log".
object Log {
    inline fun w(msg: String) = Timber.w(msg)
    inline fun d(msg: String) = Timber.d(msg)
    inline fun v(msg: String) = Timber.v(msg)
    inline fun i(msg: String) = Timber.i(msg)
    inline fun e(msg: String, throwable: Throwable? = null) = Timber.e(msg, throwable)
}
