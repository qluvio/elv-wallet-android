package app.eluvio.wallet.mocks

import okhttp3.Request
import okhttp3.Response

interface Mocker {
    fun canHandle(path: String): Boolean

    fun mock(request: Request): Response
}
