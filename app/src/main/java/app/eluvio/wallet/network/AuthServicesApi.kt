package app.eluvio.wallet.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface AuthServicesApi {
    @POST
    fun authdLogin(
        @Url url: String,
        @Body body: LoginBody = LoginBody()
    ): Single<AuthTokenResponse>

    @POST
    fun authdSign(
        @Url url: String,
        @Body body: SignBody
    ): Single<SignResponse>
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
    @field:Json(name = "token") val clusterToken: String,
)

@JsonClass(generateAdapter = true)
data class SignBody(val hash: String)

@JsonClass(generateAdapter = true)
data class SignResponse(
    @field:Json(name = "sig") val signature: String,
    // It seems that [sig] works fine, but Wayne says that's not always the case and we need to do some magic with r, s, and v to get a valid fabric token. Leaving this here as a reminder to figure out what's going on.
    //    val v: String,
    //    val s: String,
    //    val r: String,
)

//@JsonClass(generateAdapter = true)
//data class WalletDataResponse(
//    @field:Json(name = "contents") val nfts: List<Nft>,
//    @field:Json(name = "paging") val paging: Paging,
//)
