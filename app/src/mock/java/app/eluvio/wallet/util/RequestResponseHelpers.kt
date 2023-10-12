package app.eluvio.wallet.util

import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

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
