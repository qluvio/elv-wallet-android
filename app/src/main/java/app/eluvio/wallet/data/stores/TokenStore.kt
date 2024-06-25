@file:OptIn(ExperimentalCoroutinesApi::class)

package app.eluvio.wallet.data.stores

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import androidx.datastore.rxjava3.RxDataStore
import app.eluvio.wallet.util.base58
import app.eluvio.wallet.util.rx.optionalMap
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = RxPreferenceDataStoreBuilder(context, "token_store").build()

    val idToken = ReadWritePref("id_token")
    val accessToken = ReadWritePref("access_token")
    val refreshToken = ReadWritePref("refresh_token")

    val clusterToken = ReadWritePref("cluster_token")

    val fabricToken = ReadWritePref("fabric_token")

    val walletAddress = ReadWritePref("wallet_address")
    val userId: String? get() = walletAddress.get()?.let { "iusr${it.base58}" }

    val isLoggedIn: Boolean get() = fabricToken.get() != null
    val loggedInObservable = fabricToken.observe().map { it.isPresent }

    /**
     * Update multiple preferences at once.
     * This is more performant than updating them one by one.
     */
    fun update(vararg pairs: Pair<ReadWritePref, String?>) = dataStore.edit {
        pairs.forEach { (key, value) ->
            if (value != null) {
                set(key.key, value)
            } else {
                remove(key.key)
            }
        }
    }

    fun wipe() {
        dataStore.edit { clear() }
    }

    inner class ReadWritePref(keyName: String) {
        // Could be made generic to work with any type, but for now we only need strings.
        internal val key = stringPreferencesKey(keyName)

        fun observe() = dataStore.data().optionalMap { it[key] }

        fun get(): String? = observe().blockingFirst().orDefault(null)

        fun set(value: String?) {
            dataStore.edit {
                if (value != null) {
                    set(key, value)
                } else {
                    remove(key)
                }
            }
        }
    }
}

/**
 * Sync version of [RxDataStore.updateDataAsync].
 */
private fun RxDataStore<Preferences>.edit(action: MutablePreferences.() -> Unit) {
    updateDataAsync {
        val prefs = it.toMutablePreferences()
        prefs.action()
        Single.just(prefs)
    }.blockingSubscribe()
}
