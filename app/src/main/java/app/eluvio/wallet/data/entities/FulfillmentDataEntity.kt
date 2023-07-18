package app.eluvio.wallet.data.entities

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlin.reflect.KClass

class FulfillmentDataEntity : RealmObject {
    @PrimaryKey
    var transactionHash: String = ""
    var message: String? = null
    var url: String? = null
    var code: String? = null

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out BaseRealmObject> = FulfillmentDataEntity::class
    }
}
