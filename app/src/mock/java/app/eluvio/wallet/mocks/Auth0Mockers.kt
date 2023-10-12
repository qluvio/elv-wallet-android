package app.eluvio.wallet.mocks

import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.mockResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.Request
import okhttp3.Response
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

@Module
@InstallIn(SingletonComponent::class)
class Auth0MockersModule {
    @Provides
    @IntoSet
    fun provide_oauth_device_code() = object : Mocker {
        private var count = Random.nextInt(900)
        private val interval = 1 // poll every 1 second (real auth0 uses 5)

        override fun canHandle(path: String): Boolean = path == "/oauth/device/code"

        override fun mock(request: Request): Response {
            count++
            val userCode = "ABCD-${count.toString().padStart(4, '0')}"
            return request.mockResponse(
                """
                    {"device_code":"foo","user_code":"$userCode","verification_uri":"https://prod-elv.us.auth0.com/activate","expires_in":900,"interval":$interval,"verification_uri_complete":"https://prod-elv.us.auth0.com/activate?user_code=$userCode"}
                """.trimIndent()
            )
        }
    }

    @Provides
    @IntoSet
    fun provide_oauth_token() = object : Mocker {
        // Return 403 until 3 seconds have passed, then return the token
        private var startTime = Duration.ZERO

        override fun canHandle(path: String): Boolean = path == "/oauth/token"

        override fun mock(request: Request): Response {
            val now = System.nanoTime().nanoseconds
            if (startTime == Duration.ZERO) {
                startTime = now
            }
            return if (now - startTime >= 1.seconds) {
                startTime = Duration.ZERO
                Log.d("mock activation complete!")
                request.mockResponse(
                    """
                       {"access_token":"eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2R0NNIiwiaXNzIjoiaHR0cHM6Ly9wcm9kLWVsdi51cy5hdXRoMC5jb20vIn0..OAR0Zhd7oGRUWfIR.1TWVxozM_kQD8gRrS90Zj3uHbAxOMTAu2RPnk9xLJjQrJFQPTxL3HzeBw9QWq_AM1k_SbY2edARAKKx1U3reghdElBR5sci7cGt1Z5xM5icKFX-PVxzUBAz3l0saj0KQ36WGtFcsNCVfiG_9qNOcrLItYPPpI5kggcCQas4BEf2jZdKQnjdfDXJFtMkomyniipd3uN6jdnrGvrAs1i-Sr8M-1OkzwZbVnNX-fuQs8Osh6L_yrXABMF4NCLGoXxW4ucGsYKoNlVBahbGGwUQthT8mgt4PDJlv_6UrxjY0Jjq9h61UlyXiQR9Uy1In5kUCdKylv49R0OwM.ATskHDSVC2_y9pmNDxrNcA","refresh_token":"v1.Mb-5EndpT6WrkFqdrVT4kG4dh2LK-jjUEpPpDgm_IOkT4FgM_sKpoyEvhWh7WDGFujByx-SKBft5wmqbHmMeaXI","id_token":"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Inlwd1ZUbXJkWENkYU5tcjAzVGRDaCJ9.eyJodHRwczovL2F1dGguY29udGVudGZhYnJpYy5pby9nZW8iOnsiY291bnRyeV9jb2RlIjoiVVMiLCJjb3VudHJ5X2NvZGUzIjoiVVNBIiwiY291bnRyeV9uYW1lIjoiVW5pdGVkIFN0YXRlcyIsImNpdHlfbmFtZSI6Ik9ha2xhbmQiLCJsYXRpdHVkZSI6MzcuODA4NCwibG9uZ2l0dWRlIjotMTIyLjI4NDYsInRpbWVfem9uZSI6IkFtZXJpY2EvTG9zX0FuZ2VsZXMiLCJjb250aW5lbnRfY29kZSI6Ik5BIiwic3ViZGl2aXNpb25fY29kZSI6IkNBIiwic3ViZGl2aXNpb25fbmFtZSI6IkNhbGlmb3JuaWEifSwibmlja25hbWUiOiJzdGF2IiwibmFtZSI6InN0YXZAZWx1di5pbyIsInBpY3R1cmUiOiJodHRwczovL3MuZ3JhdmF0YXIuY29tL2F2YXRhci8zMTM2OTk1MmQ3NmRmYjA2NzY2ZDUyYmIxZmM4NDA0Zj9zPTQ4MCZyPXBnJmQ9aHR0cHMlM0ElMkYlMkZjZG4uYXV0aDAuY29tJTJGYXZhdGFycyUyRnN0LnBuZyIsInVwZGF0ZWRfYXQiOiIyMDIzLTEwLTExVDIyOjU0OjA3LjA2NVoiLCJlbWFpbCI6InN0YXZAZWx1di5pbyIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiaXNzIjoiaHR0cHM6Ly9wcm9kLWVsdi51cy5hdXRoMC5jb20vIiwiYXVkIjoiTzF0clJhVDhuQ3BMa2U5ZTM3UDk4Q3M5WThOTHBvYXIiLCJpYXQiOjE2OTcxMzYwMDYsImV4cCI6MTY5NzE3MjAwNiwic3ViIjoiYXV0aDB8NjUxYzZmM2M0MWQ1OTMzMTUwNTQzZmQ2In0.Jhr25uNlwmQ3PlHuZ3YxywmaCgwZHfHwuc3ZQDtNPdiONH3U9HTmjzGwTFp5wKixiGFrubQRRN5HeWpugEHig6q6c7csISxA7P4GQCwxpoXreC6PBw_4qx4r8s_gnhQUv75hppLFmrMcyG2a4K1XuLy1J4YcPxybsnZ3OAcNFySXyLYyM_ZvOBFriFrlXywqFB6SSI0y9M9ym2OYEMVbw8Ru7mhXkqxLoqBzMgXOpMSKI_7WYqO5c3pfn96HhNkVikifQNK6JL93ymT-YKqDL_u2_uAwxA8tTpH0QMUrSM0aswjACYeZew7UFp4QuxpECVsrLazoIzIaR3AlryTKKw","scope":"openid profile email offline_access","expires_in":86400,"token_type":"Bearer"}                        
                    """.trimIndent()
                )
            } else {
                Log.d("mock activation still pending...")
                request.mockResponse(
                    """
                        {"error":"authorization_pending","error_description":"User has yet to authorize device code."}                        
                    """.trimIndent(),
                    403
                )
            }
        }
    }
}
