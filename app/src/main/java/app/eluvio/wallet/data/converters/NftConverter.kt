package app.eluvio.wallet.data.converters

import app.eluvio.wallet.network.NftResponse
import app.eluvio.wallet.sqldelight.Nft


fun NftResponse.toNfts(): List<Nft> {
    return contents.map { dto ->
        Nft(
            id = "${dto.contract_addr}_${dto.token_id}",
            contract_addr = dto.contract_addr,
            token_id = dto.token_id,
            imageUrl = dto.meta.image,
            display_name = dto.meta.display_name,
            edition_name = dto.meta.edition_name ?: "",
            description = dto.meta.description ?: ""
        )
    }
}
