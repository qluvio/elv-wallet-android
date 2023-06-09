package app.eluvio.wallet.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Paging(
    val start: Int,
    val limit: Int,
    val total: Int,
)
