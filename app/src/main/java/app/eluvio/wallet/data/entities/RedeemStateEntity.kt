package app.eluvio.wallet.data.entities

import app.eluvio.wallet.util.realm.RealmEnum
import app.eluvio.wallet.util.realm.realmEnum
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.annotations.Ignore
import kotlin.reflect.KClass

class RedeemStateEntity : EmbeddedRealmObject {
    var offerId: String = ""
    var active: Boolean = false
    var redeemer: String? = null
    var redeemed: RealmInstant? = null
    var transaction: String? = null
    private var statusStr: String = Status.UNREDEEMED.value

    @Ignore
    var status: Status by realmEnum(::statusStr)

    enum class Status(override val value: String) : RealmEnum {
        UNREDEEMED("UNREDEEMED"),
        REDEEMING("REDEEMING"),
        REDEEMED("REDEEMED"),
        REDEEM_FAILED("REDEEM_FAILED")
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out BaseRealmObject> = RedeemStateEntity::class
    }
}
