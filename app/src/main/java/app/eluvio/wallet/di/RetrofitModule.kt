package app.eluvio.wallet.di

import app.eluvio.wallet.data.TokenStore
import app.eluvio.wallet.network.Auth0Api
import app.eluvio.wallet.network.AuthServicesApi
import app.eluvio.wallet.network.FabricConfigApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {
    @Singleton
    @Provides
    @Fabric
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://localhost/")
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
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
    }

    @Singleton
    @Provides
    @AuthD
    fun provideAuthDRetrofit(tokenStore: TokenStore): Retrofit {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val builder = request.newBuilder()
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
            .build()
        return Retrofit.Builder()
            .baseUrl("https://localhost")
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
    }

    @Provides
    fun provideConfigApi(@Fabric retrofit: Retrofit): FabricConfigApi = retrofit.create()

    @Provides
    fun provideAuthServicesApi(@AuthD retrofit: Retrofit): AuthServicesApi = retrofit.create()

    @Provides
    fun provideAuth0Api(@Auth0 retrofit: Retrofit): Auth0Api = retrofit.create()
}

@Qualifier
annotation class Auth0

@Qualifier
annotation class AuthD

@Qualifier
annotation class Fabric