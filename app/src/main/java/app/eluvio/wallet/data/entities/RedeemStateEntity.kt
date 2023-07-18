package app.eluvio.wallet.data.entities

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmInstant
import kotlin.reflect.KClass

class RedeemStateEntity : EmbeddedRealmObject {
    var offerId: String = ""
    var active: Boolean = false
    var redeemer: String? = null
    var redeemed: RealmInstant? = null
    var transaction: String? = null

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out BaseRealmObject> = RedeemStateEntity::class
    }
}
