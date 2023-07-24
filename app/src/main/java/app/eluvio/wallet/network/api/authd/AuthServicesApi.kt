package app.eluvio.wallet.network.api.authd

import app.eluvio.wallet.data.entities.SelectedEnvEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthServicesApi : AuthdApi {
    @POST("wlt/login/jwt")
    fun authdLogin(
        @Body body: LoginBody = LoginBody()
    ): Single<AuthTokenResponse>

    @POST("wlt/sign/eth/{accountId}")
    fun authdSign(
        @Path("accountId") accountId: String,
        @Body body: SignBody
    ): Single<SignResponse>

    @POST("wlt/login/redirect/metamask")
    fun generateMetamaskCode(@Body body: MetamaskCodeRequest): Single<MetamaskActivationData>

    @GET("wlt/login/redirect/metamask/{code}/{passcode}")
    fun getMetamaskToken(
        @Path("code") code: String,
        @Path("passcode") passcode: String,
    ): Single<Response<MetamaskTokenResponse>>
}

@JsonClass(generateAdapter = true)
data class LoginBody(
    val ext: Ext = Ext()
)

@JsonClass(generateAdapter = true)
data class Ext(
    @field:Json(name = "share_email") val shareEmail: Boolean = true
)

@JsonClass(generateAdapter = true)
data class AuthTokenResponse(
    @field:Json(name = "addr") val address: String,
    // Custodial wallets will return a cluster token, and there will be additional steps to create a fabric token from it.
    // Metamask will return a fabric token directly.
    @field:Json(name = "token") val clusterToken: String,
)

@JsonClass(generateAdapter = true)
data class SignBody(val hash: String)

@JsonClass(generateAdapter = true)
data class SignResponse(
    @field:Json(name = "sig") val signature: String,
)

@JsonClass(generateAdapter = true)
data class MetamaskCodeRequest(
    @field:Json(name = "dest") val walletUrl: String,
    val op: String = "create",
) {
    companion object {
        fun from(environment: SelectedEnvEntity.Environment) =
            MetamaskCodeRequest(walletUrl = environment.walletUrl)
    }
}

@JsonClass(generateAdapter = true)
data class MetamaskActivationData(
    @field:Json(name = "id") val code: String,
    @field:Json(name = "passcode") val passcode: String,
    @field:Json(name = "url") val url: String,
    @field:Json(name = "metamask_url") val metamaskUrl: String,
    @field:Json(name = "expiration") val expiration: Long,
)

@JsonClass(generateAdapter = true)
data class MetamaskTokenResponse(
    // [payload] holds a string, but it can be parsed into [AuthTokenResponse]
    val payload: String,
)
