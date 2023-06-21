package app.eluvio.wallet.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.BaseRealmObject
import kotlin.reflect.KClass

@InstallIn(SingletonComponent::class)
@Module
object RealmModule {
    @Provides
    fun provideRealm(realmObjects: Set<@JvmSuppressWildcards KClass<out BaseRealmObject>>): Realm {
        // TODO: encrypt db
        val config = RealmConfiguration.Builder(realmObjects)
            .deleteRealmIfMigrationNeeded()
            .build()
        return Realm.open(config)
    }

    @Provides
    @ElementsIntoSet
    fun provideRealmObjects(): Set<KClass<out BaseRealmObject>> = emptySet()
}
