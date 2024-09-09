package app.eluvio.wallet.util.datastore

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.rxjava3.RxDataStore
import app.eluvio.wallet.util.rx.optionalMap
import kotlinx.coroutines.ExperimentalCoroutinesApi

fun RxDataStore<Preferences>.readWriteStringPref(keyName: String) =
    ReadWritePref(this, stringPreferencesKey(keyName))

fun RxDataStore<Preferences>.readWriteBoolPref(keyName: String) =
    ReadWritePref(this, booleanPreferencesKey(keyName))

@OptIn(ExperimentalCoroutinesApi::class)
class ReadWritePref<T : Any>(
    private val dataStore: RxDataStore<Preferences>,
    private val key: Preferences.Key<T>,
) {
    fun observe() = dataStore.data().optionalMap { it[key] }

    fun get(): T? = observe().blockingFirst().orDefault(null)

    fun set(value: T?, editor: MutablePreferences) {
        if (value != null) {
            editor[key] = value
        } else {
            editor.remove(key)
        }
    }

    fun set(value: T?) {
        dataStore.edit {
            set(value, this)
        }
    }

    /**
     * Create an operation to set the value of this preference without executing it, allowing to
     * batch multiple operations together.
     */
    infix fun to(value: T?): StoreOperation = { set(value, this) }
}

typealias StoreOperation = MutablePreferences.() -> Unit
