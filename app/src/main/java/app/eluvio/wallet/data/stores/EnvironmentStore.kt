package app.eluvio.wallet.data.stores

import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import app.eluvio.wallet.R
import app.eluvio.wallet.util.datastore.readWriteStringPref
import app.eluvio.wallet.util.rx.mapNotNull
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject
import javax.inject.Singleton

enum class Environment(
    val networkName: String,
    @StringRes val prettyEnvName: Int,
    val configUrl: String,
    val walletUrl: String,
) {
    Main(
        networkName = "main",
        prettyEnvName = R.string.env_main_name,
        configUrl = "https://main.net955305.contentfabric.io/config",
        walletUrl = "https://wallet.contentfabric.io"
    ),
    Demo(
        networkName = "demov3",
        prettyEnvName = R.string.env_demo_name,
        configUrl = "https://demov3.net955210.contentfabric.io/config",
        walletUrl = "https://wallet.demov3.contentfabric.io"
    )
}

@Singleton
class EnvironmentStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dataStore = RxPreferenceDataStoreBuilder(context, "selected_env_store").build()
    private val selectedEnvName = dataStore.readWriteStringPref("selected_env")

    fun setSelectedEnvironment(environment: Environment) {
        selectedEnvName.set(environment.networkName)
    }

    fun observeSelectedEnvironment(): Flowable<Environment> {
        return selectedEnvName.observe()
            .mapNotNull { optionalEnv ->
                val envName = optionalEnv.orDefault(null)
                val environment = Environment.entries
                    .firstOrNull { it.networkName == envName }
                if (environment == null) {
                    // No env set. Default to Main
                    setSelectedEnvironment(Environment.Main)
                }
                environment
            }
    }
}
