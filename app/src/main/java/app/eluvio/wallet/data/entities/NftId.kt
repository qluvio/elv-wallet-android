package app.eluvio.wallet.data.entities

/**
 * NFT templates can come from:
 * 1) apigw/nfts, where every nft_template is tied to a specific token the user owns, thus the
 *    primary key is the same as the NftEntity's primary key: contractAddress+tokenId
 * 2) A marketplace SKU, where the primary key is the marketplaceId+SKU
 * @see [NftId]
 */
object NftId {
    fun forToken(contractAddress: String, tokenId: String): String = "${contractAddress}_${tokenId}"
    fun forSku(marketplace: String, sku: String): String = "${marketplace}_${sku}"
}
