package app.eluvio.wallet.util.logging

import android.util.Log

object Log {
    private const val TAG = "EluvioMediaWallet"
    fun w(msg: String) = Log.w(TAG, msg)
    fun d(msg: String) = Log.d(TAG, msg)
    fun v(msg: String) = Log.v(TAG, msg)
    fun i(msg: String) = Log.i(TAG, msg)
    fun e(msg: String, throwable: Throwable? = null) = Log.e(TAG, msg, throwable)
}