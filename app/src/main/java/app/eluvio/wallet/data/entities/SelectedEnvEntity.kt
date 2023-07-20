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
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import kotlin.reflect.KClass

class SelectedEnvEntity : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()
    private var selectedEnvStr: String = Environment.Main.value

    @Ignore
    var selectedEnv: Environment by realmEnum(::selectedEnvStr)

    enum class Environment(
        override val value: String,
        @StringRes val prettyEnvName: Int,
        val configUrl: String,
        val networkName: String = value
    ) : RealmEnum {
        Main("main", R.string.env_main_name, "https://main.net955305.contentfabric.io/config"),
        Demo("demov3", R.string.env_demo_name, "https://demov3.net955210.contentfabric.io/config"),
        ;
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out BaseRealmObject> = SelectedEnvEntity::class
    }
}
