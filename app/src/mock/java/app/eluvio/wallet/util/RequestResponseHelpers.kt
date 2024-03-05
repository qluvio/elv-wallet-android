package app.eluvio.wallet.util

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> Request.mockResponse(
    body: T,
    moshi: Moshi,
    statusCode: Int = 200
): Response {
    val json = moshi.adapter<T>().toJson(body)
    return mockResponse(json, statusCode)
}

fun Request.mockResponse(body: String, statusCode: Int = 200): Response {
    return Response.Builder()
        .code(statusCode)
        .message(body)
        .request(this)
        .protocol(Protocol.HTTP_1_0)
        .body(body.toResponseBody())
        .addHeader("content-type", "application/json")
        .build()
}
