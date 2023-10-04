package app.eluvio.wallet.data.entities

import androidx.annotation.StringRes
import app.eluvio.wallet.R
import app.eluvio.wallet.util.realm.RealmEnum
import app.eluvio.wallet.util.realm.realmEnum
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.TypedRealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlin.reflect.KClass

class SelectedEnvEntity : RealmObject {
    enum class Environment(
        override val value: String,
        @StringRes val prettyEnvName: Int,
        val configUrl: String,
        val walletUrl: String,
        val networkName: String = value
    ) : RealmEnum {
        Main(
            "main",
            R.string.env_main_name,
            "https://main.net955305.contentfabric.io/config",
            "https://wallet.contentfabric.io?action=login&mode=login&response=code&source=code#/login"
        ),
        Demo(
            "demov3",
            R.string.env_demo_name,
            "https://demov3.net955210.contentfabric.io/config",
            "https://wallet.demov3.contentfabric.io?action=login&mode=login&response=code&source=code#/login"
        ),
        ;
    }

    @PrimaryKey
    var id: String = "singleton"
    private var selectedEnvStr: String = Environment.Main.value

    @Ignore
    var selectedEnv: Environment by realmEnum(::selectedEnvStr)

    override fun toString(): String {
        return "SelectedEnvEntity(id=$id, selectedEnvStr='$selectedEnvStr')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SelectedEnvEntity

        if (id != other.id) return false
        if (selectedEnvStr != other.selectedEnvStr) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + selectedEnvStr.hashCode()
        return result
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out TypedRealmObject> = SelectedEnvEntity::class
    }
}
