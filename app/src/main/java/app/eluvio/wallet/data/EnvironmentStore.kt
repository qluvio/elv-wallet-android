package app.eluvio.wallet.data

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.core.content.edit
import app.eluvio.wallet.R
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
            prefs.edit { putString(KEY_SELECTED_ENV, Environment.Main.name) }
        }
        onEnvChanged()
        listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                KEY_SELECTED_ENV -> onEnvChanged()
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    private fun onEnvChanged() {
        // hidden bug -> if the key is removed, we will ignore it and our Subject will still contain the old value
        prefs.getString(KEY_SELECTED_ENV, null)
            ?.runCatching { Environment.valueOf(this) }
            ?.onSuccess {
                if (it != selectedEnvSubject.value) {
                    selectedEnvSubject.onNext(it)
                }
            }
    }

    fun observeSelectedEnvironment(): Observable<Environment> =
        selectedEnvSubject.distinctUntilChanged()

    fun setSelectedEnvironment(environment: Environment) {
        prefs.edit { putString(KEY_SELECTED_ENV, environment.name) }
    }

    companion object {
        private const val KEY_SELECTED_ENV = "selected_env"
    }
}

// todo proguard rules?
enum class Environment(
    @StringRes val envName: Int,
    val configUrl: String
) {
    Main(R.string.env_main_name, "https://main.net955305.contentfabric.io/config"),
    Demo(R.string.env_demo_name, "https://demov3.net955210.contentfabric.io/config"),
    ;
}
