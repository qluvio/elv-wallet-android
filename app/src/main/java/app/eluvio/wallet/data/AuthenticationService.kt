package app.eluvio.wallet.data

import android.util.Base64
import app.eluvio.wallet.network.AuthServicesApi
import app.eluvio.wallet.network.SignBody
import app.eluvio.wallet.util.Base58
import app.eluvio.wallet.util.Keccak
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.mapNotNull
import app.eluvio.wallet.util.toHexByteArray
import io.reactivex.rxjava3.core.Single
import java.util.Date
import java.util.zip.Deflater
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.days

@Singleton
class AuthenticationService @Inject constructor(
    private val authServicesApi: AuthServicesApi,
    private val fabricConfigStore: FabricConfigStore,
    private val tokenStore: TokenStore,
    private val userStore: UserStore,
) {
    fun getFabricToken(idToken: String): Single<String> {
        return fabricConfigStore.observeFabricConfiguration()
            .firstOrError()
            .flatMap { fabricConfig ->
                val authBaseUrl = fabricConfig.network.services.authService.first()
                val url = "$authBaseUrl$WALLET_JWT_LOGIN_PATH"
                Log.d("fetching fabric token from: $url  id_token=${idToken}")
                authServicesApi.authdLogin(url)
                    .doOnSuccess {
                        Log.d("login response: $it")
                        tokenStore.clusterToken = it.clusterToken
                        userStore.saveUser(it.address)
                    }
                    .flatMap { jwtResponse ->
                        val (accountId, hash, tokenString) = createTokenParts(
                            jwtResponse.address,
                            fabricConfig.qspace.id
                        )
                        remoteSign(hash, accountId, authBaseUrl)
                            .map { signature ->
                                createFabricToken(tokenString, signature).also {
                                    tokenStore.fabricToken = it
                                }
                            }
                    }
            }
    }

    fun getWalletData() {
//        fabricConfigStore.observeFabricConfiguration()
//            .firstOrError()
//            .mapNotNull { fabricConfig ->
//                tokenStore.accountId?.let { accountId ->
//                    val authBaseUrl = fabricConfig.network.services.authService.first()
//                    "$authBaseUrl$WALLET_DATA_PATH$accountId"
//                }
//            }
//            .flatMapSingle { url ->
//                authServicesApi.getWalletData(url)
//            }
    }

    private fun createFabricToken(tokenString: String, signature: String): String {
        val compressedToken = tokenString.zlibCompress()
        val bytes = signature.toHexByteArray() + compressedToken
        return "acspjc${Base58.encode(bytes)}".also {
            Log.d("fabric token: $it")
        }
    }

    private fun createTokenParts(address: String, qspace: String): TokenParts {
        val addressBytes = address.toHexByteArray()
        val base64Address = Base64.encodeToString(addressBytes, Base64.DEFAULT)
        val base58Address = Base58.encode(addressBytes)
        val sub = "iusr${base58Address}"
        val duration = 7.days
        val accountId = "ikms${base58Address}"

        val tokenString = """
            {
            "sub": "$sub",
            "adr": "$base64Address",
            "spc": "$qspace",
            "iat": ${Date().time},
            "exp": ${Date().time + duration.inWholeMilliseconds}
            }
        """.replace(Regex("\\n|\\s"), "")
        Log.d("tokenString before signing: $tokenString")

        val hash = keccak256("Eluvio Content Fabric Access Token 1.0\n$tokenString").toHexString()
        Log.d("eth msg hash: $hash")
        return TokenParts(accountId, hash, tokenString)
    }

    data class TokenParts(val accountId: String, val hash: String, val tokenString: String)

    /**
     * [accountId] is a base58 encoded string of the address prefixed with "ikms"
     */
    private fun remoteSign(
        hash: String,
        accountId: String,
        authServiceUrl: String
    ): Single<String> {
        val url = "$authServiceUrl$WALLET_SIGN_PATH$accountId"
        Log.d("remote signing hash: $hash  url: $url token: ${tokenStore.clusterToken}")
        return authServicesApi.authdSign(url, SignBody(hash))
            .map { it.signature }
            .doOnSuccess { Log.d("Signature obtained: $it") }
    }

    private fun keccak256(message: String): ByteArray {
        return Keccak(256).apply {
            update("\u0019Ethereum Signed Message:\n${message.length}$message".toByteArray())
        }.digestArray()
    }

    private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

    private fun String.zlibCompress(): ByteArray {
        val input = this.toByteArray()
        val output = ByteArray(input.size * 4)
        val noWrap = true // This is *EXTREMELY* important, otherwise you'll get the wrong results
        val compressor = Deflater(Deflater.DEFAULT_COMPRESSION, noWrap).apply {
            setInput(input)
            finish()
        }
        val compressedDataLength = compressor.deflate(output)
        compressor.end()
        return output.copyOfRange(0, compressedDataLength)
    }

    companion object {
        private const val WALLET_JWT_LOGIN_PATH = "/wlt/login/jwt"
        private const val WALLET_SIGN_PATH = "/wlt/sign/eth/"
        private const val WALLET_DATA_PATH = "/wlt/"
    }
}
