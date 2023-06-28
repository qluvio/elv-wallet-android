package app.eluvio.wallet.di

import app.eluvio.wallet.data.stores.TokenStore
import app.eluvio.wallet.network.Auth0Api
import app.eluvio.wallet.network.FabricConfigApi
import app.eluvio.wallet.network.adapters.AssetLinkAdapter
import com.squareup.moshi.Moshi
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
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {
    @Provides
    @TokenAwareHttpClient
    fun provideHttpClient(tokenStore: TokenStore): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val builder = request.newBuilder()
                builder.header("Accept", "*/*") // needed for link/file resolution from the fabric
                if (request.url.toString().endsWith("wlt/login/jwt")) {
                    tokenStore.idToken?.let { idToken ->
                        builder.header("Authorization", "Bearer $idToken")
                    }
                } else if (request.url.toString().contains("wlt/sign/eth")) {
                    tokenStore.clusterToken?.let { walletToken ->
                        builder.header("Authorization", "Bearer $walletToken")
                    }
                } else {
                    tokenStore.fabricToken?.let { fabricToken ->
                        builder.header("Authorization", "Bearer $fabricToken")
                    }
                }
                chain.proceed(builder.build())
            }
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }

    @Provides
    fun provideRetrofitBuilder(@TokenAwareHttpClient httpClient: OkHttpClient): Retrofit.Builder {
        val moshi = Moshi.Builder()
            .add(AssetLinkAdapter())
            .build()
        return Retrofit.Builder()
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
    }

    @Singleton
    @Provides
    @FabricConfig
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://localhost/")
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build()
            )
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
    }

    @Singleton
    @Provides
    @Auth0
    fun provideAuth0Retrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://prod-elv.us.auth0.com/")
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build()
            )
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
    }

    @Singleton
    @Provides
    @AuthD
    fun provideAuthDRetrofit(retrofitBuilder: Retrofit.Builder): Retrofit {
        return retrofitBuilder.baseUrl("https://localhost/").build()
    }

    @Provides
    fun provideConfigApi(@FabricConfig retrofit: Retrofit): FabricConfigApi = retrofit.create()

    @Provides
    fun provideAuth0Api(@Auth0 retrofit: Retrofit): Auth0Api = retrofit.create()
}

@Qualifier
annotation class Auth0

@Qualifier
annotation class AuthD

@Qualifier
annotation class FabricConfig

@Qualifier
annotation class TokenAwareHttpClient
