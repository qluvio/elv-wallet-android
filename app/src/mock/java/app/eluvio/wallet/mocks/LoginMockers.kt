package app.eluvio.wallet.mocks

import app.eluvio.wallet.network.api.authd.AuthTokenResponse
import app.eluvio.wallet.util.mockResponse
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.Request
import okhttp3.Response

@Module
@InstallIn(SingletonComponent::class)
class LoginMockersModule {
    @Provides
    @IntoSet
    fun provide_as_wlt_login_jwt(moshi: Moshi) = object : Mocker {
        override fun canHandle(path: String): Boolean = path == "/as/wlt/login/jwt"

        override fun mock(request: Request): Response {
            return request.mockResponse(
                AuthTokenResponse(
                    "0x558E4d37a7B81cd07734553951BD1E68A8EFc412",
                    "eyJ2ZXIiOjEsImtpZCI6ImlrZXkyRmdjMmNHTXlYazhMNk5IWHhOcHM1QzU4NEFKIiwia2V5IjoiQXcyNDBZQncrMTMweEl0cmZCTy8xZGNJbmthd3FFcXVFOXlEMEdVRmdncDdCY1dYWjFVNTZqY2QrTHk1Y2k3WFVnPT0iLCJpZCI6Im1pY2hlbGxlK2FwcGxlQGVsdXYuaW8iLCJpYXQiOjE2OTcxNDgyODkxMTQsImV4cCI6MTY5NzIzNDY4OTExNH0"
                ), moshi
            )
        }
    }

    @Provides
    @IntoSet
    fun provide_as_wlt_sign_eth(moshi: Moshi) = object : Mocker {
        override fun canHandle(path: String): Boolean {
            return path.contains("/as/wlt/sign/eth/")
        }

        override fun mock(request: Request): Response {
            return request.mockResponse(
                """
                {"sig":"0x4f06965d42eca3057ae371bedd2105e64d6950c5239b32e7eed624d5e54d75405a23ab09c3153e95e591866fece96ed2e43e8eab60399b3bc3264b900d5541d401", "v":"0x1c", "r":"0x4f06965d42eca3057ae371bedd2105e64d6950c5239b32e7eed624d5e54d7540", "s":"0x5a23ab09c3153e95e591866fece96ed2e43e8eab60399b3bc3264b900d5541d4"}
            """.trimIndent()
            )
        }
    }
}
