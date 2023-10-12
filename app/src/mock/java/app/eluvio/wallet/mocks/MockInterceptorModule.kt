package app.eluvio.wallet.mocks

import app.eluvio.wallet.network.dto.FabricConfiguration
import app.eluvio.wallet.network.dto.Network
import app.eluvio.wallet.network.dto.QSpace
import app.eluvio.wallet.network.dto.Services
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.mockResponse
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

@Module
@InstallIn(SingletonComponent::class)
object MockInterceptorModule {
    @Provides
    @IntoSet
    fun provideMockInterceptor(mockers: Set<@JvmSuppressWildcards Mocker>): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val path = request.url.encodedPath
            val mocker = mockers.firstOrNull { it.canHandle(path) }
            if (mocker != null) {
                mocker.mock(request)
            } else {
                Log.w("un-mocked request path: $path")
                chain.proceed(request)
            }
        }
    }

    @Provides
    @IntoSet
    fun provide_config(moshi: Moshi): Mocker = object : Mocker {
        override fun canHandle(path: String): Boolean = path == "/config"

        override fun mock(request: Request): Response {
            val baseUrl = "https://elv.mock"
            val mockConfig = FabricConfiguration(
                nodeID = "le_mock",
                network = Network(
                    apiVersions = emptyList(),
                    services = Services(
                        authService = listOf("$baseUrl/as"),
                        ethereumApi = listOf("$baseUrl/eth/"),
                        fabricApi = listOf(baseUrl),
                    )
                ),
                qspace = QSpace("mock", "1.0", "mock", listOf("mock")),
            )
            return request.mockResponse(mockConfig, moshi)
        }
    }
}
