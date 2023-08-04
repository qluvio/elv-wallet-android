package app.eluvio.wallet.di

import app.eluvio.wallet.network.adapters.AssetLinkAdapter
import app.eluvio.wallet.network.api.Auth0Api
import app.eluvio.wallet.network.api.FabricConfigApi
import app.eluvio.wallet.network.interceptors.AccessTokenInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.addAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import timber.log.Timber
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {
    @Provides
    @TokenAwareHttpClient
    fun provideHttpClient(
        accessTokenInterceptor: AccessTokenInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(accessTokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    fun provideRetrofitBuilder(
        @TokenAwareHttpClient httpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit.Builder {
        return Retrofit.Builder()
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addAdapter(Rfc3339DateJsonAdapter())
            .add(AssetLinkAdapter())
            .build()
    }

    @Singleton
    @Provides
    @FabricConfig
    fun provideRetrofit(moshi: Moshi, loggingInterceptor: HttpLoggingInterceptor): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://localhost/")
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build()
            )
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
    }

    @Singleton
    @Provides
    @Auth0
    fun provideAuth0Retrofit(moshi: Moshi, loggingInterceptor: HttpLoggingInterceptor): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://prod-elv.us.auth0.com/")
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build()
            )
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
    }

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Timber.tag("OkHttp").d(message)
        }
            .setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @Provides
    fun provideConfigApi(@FabricConfig retrofit: Retrofit): FabricConfigApi = retrofit.create()

    @Provides
    fun provideAuth0Api(@Auth0 retrofit: Retrofit): Auth0Api = retrofit.create()
}

@Qualifier
annotation class Auth0

@Qualifier
annotation class FabricConfig

@Qualifier
annotation class TokenAwareHttpClient
