package app.eluvio.wallet.screens.nftdetail

data class NftDetailArgs(
    val contractAddress: String,
    val tokenId: String,
    val marketplaceId: String? = null,
    val backLink: String? = null
)
