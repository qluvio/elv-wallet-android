package app.eluvio.wallet.util.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.rxjava3.RxDataStore
import app.eluvio.wallet.util.rx.optionalMap
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class ReadWriteStringPref(
    private val dataStore: RxDataStore<Preferences>,
    keyName: String
) {
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

fun RxDataStore<Preferences>.readWriteStringPref(keyName: String) = ReadWriteStringPref(this, keyName)
