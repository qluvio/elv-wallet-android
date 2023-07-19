package app.eluvio.wallet.data.stores

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.core.content.edit
import app.eluvio.wallet.R
import app.eluvio.wallet.util.logging.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnvironmentStore @Inject constructor(@ApplicationContext private val context: Context) {
    // todo: encrypt
    private val prefs = context.getSharedPreferences("env_prefs", Context.MODE_PRIVATE)
    private val selectedEnvSubject = BehaviorSubject.create<Environment>()

    /**
     * Holding a strong ref to the listener to keep it from being GC'd. The android SDK only keeps a weak ref
     */
    private val listener: SharedPreferences.OnSharedPreferenceChangeListener

    init {
        // there's a bug here. make sure the env also has the correct value
        if (KEY_SELECTED_ENV !in prefs) {
            prefs.edit { putString(KEY_SELECTED_ENV, Environment.Main.properEnvName) }
        }
        onEnvChanged()
        listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_SELECTED_ENV -> onEnvChanged()
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    private fun onEnvChanged() {
        // hidden bug -> if the key is removed, we will ignore it and our Subject will still contain the old value
        prefs.getString(KEY_SELECTED_ENV, null)
            ?.runCatching { Environment.fromProperName(this) }
            ?.onSuccess { env ->
                if (env != null && env != selectedEnvSubject.value) {
                    Log.d("Emitting new selected env: $env")
                    selectedEnvSubject.onNext(env)
                }
            }
    }

    fun observeSelectedEnvironment(): Observable<Environment> =
        selectedEnvSubject.distinctUntilChanged()

    fun setSelectedEnvironment(environment: Environment) {
        prefs.edit {
            Log.d("Setting env to $environment")
            putString(KEY_SELECTED_ENV, environment.properEnvName)
        }
    }

    companion object {
        private const val KEY_SELECTED_ENV = "selected_env"
    }
}

enum class Environment(
    @StringRes val prettyEnvName: Int,
    val properEnvName: String,
    val configUrl: String
) {
    Main(R.string.env_main_name, "main", "https://main.net955305.contentfabric.io/config"),
    Demo(R.string.env_demo_name, "demov3", "https://demov3.net955210.contentfabric.io/config"),
    ;

    companion object {
        fun fromProperName(properName: String): Environment? =
            // TODO: replace [values()] with [entries] when using Kotlin >= 1.9
            values().find { it.properEnvName == properName }
    }
}
