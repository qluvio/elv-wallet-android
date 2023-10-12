package app.eluvio.wallet.mocks

import app.eluvio.wallet.network.dto.FabricConfiguration
import app.eluvio.wallet.network.dto.Network
import app.eluvio.wallet.network.dto.QSpace
import app.eluvio.wallet.network.dto.Services
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.mockResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import dagger.multibindings.StringKey
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

typealias Mocker = (Request) -> Response?

@Module
@InstallIn(SingletonComponent::class)
@OptIn(ExperimentalStdlibApi::class)
object MockInterceptorModule {
    @Provides
    @IntoSet
    fun provideMockInterceptor(mockers: Map<String, @JvmSuppressWildcards Mocker>): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val path = request.url.encodedPath
            val response: Response? = mockers[path]?.invoke(request)
            response ?: chain.proceed(request).also {
                Log.w("un-mocked request path: $path")
            }
        }
    }

    @Provides
    @IntoMap
    @StringKey("/config")
    fun provide_config(moshi: Moshi): Mocker = { request ->
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
        request.mockResponse(moshi.adapter<FabricConfiguration>().toJson(mockConfig))
    }
}
