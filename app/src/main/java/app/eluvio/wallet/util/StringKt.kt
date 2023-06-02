package app.eluvio.wallet.util

fun String.toHexByteArray(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }
    return removePrefix("0x")
        .chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

val String.base58: String
    get() = Base58.encode(this.toHexByteArray())
