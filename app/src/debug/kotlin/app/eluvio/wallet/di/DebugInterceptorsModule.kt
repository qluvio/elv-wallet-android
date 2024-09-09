package app.eluvio.wallet.di

import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber


@Module
@InstallIn(SingletonComponent::class)
object DebugInterceptorsModule {
    @Provides
    @IntoSet
    fun provideLoggingInterceptor(): Interceptor {
        return HttpLoggingInterceptor { message ->
            Timber.tag("OkHttp").d(message)
        }
            .setLevel(HttpLoggingInterceptor.Level.BASIC)
    }

    @Provides
    @IntoSet
    fun provideCurlInterceptor(): Interceptor {
        return CurlInterceptor(
            logger = object : Logger {
                override fun log(message: String) {
                    Timber.tag("OkHttp").w(message)
                }
            }
        )
    }
}
