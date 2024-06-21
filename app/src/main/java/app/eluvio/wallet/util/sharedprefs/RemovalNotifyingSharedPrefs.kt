package app.eluvio.wallet.util.sharedprefs

import android.content.SharedPreferences

/**
 * Convenience method to wrap a [SharedPreferences] in a [RemovalNotifyingSharedPrefs].
 */
fun SharedPreferences.toRemovalNotifyingSharedPrefs(): SharedPreferences =
    RemovalNotifyingSharedPrefs(this)

/**
 * A [SharedPreferences] wrapper that notifies listeners when a keys are removed.
 */
class RemovalNotifyingSharedPrefs(
    private val prefs: SharedPreferences
) : SharedPreferences by prefs {
    private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    override fun edit(): SharedPreferences.Editor {
        // Passing [this] instead of [prefs] specifically to avoid the rx3prefs check that compares
        // the SharedPrefs in the callback, to the one used to create the rx3prefs.
        return Editor(this, prefs.edit(), listeners)
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        listeners += listener
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        listeners -= listener
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private class Editor(
        private val prefs: SharedPreferences,
        private val editor: SharedPreferences.Editor,
        private val listeners: Set<SharedPreferences.OnSharedPreferenceChangeListener>,
    ) : SharedPreferences.Editor by editor {

        private val removedKeys = mutableSetOf<String>()

        override fun remove(key: String): SharedPreferences.Editor = apply {
            removedKeys += key
            editor.remove(key)
        }

        override fun clear(): SharedPreferences.Editor = apply {
            removedKeys += prefs.all.keys
            editor.clear()
        }

        override fun commit(): Boolean {
            try {
                return editor.commit()
            } finally {
                notifyListeners()
            }
        }

        override fun apply() {
            editor.apply()
            notifyListeners()
        }

        private fun notifyListeners() {
            removedKeys.forEach { key ->
                listeners.forEach { it.onSharedPreferenceChanged(prefs, key) }
            }
            removedKeys.clear()
        }
    }
}
