package app.eluvio.wallet.network.converters

import app.eluvio.wallet.data.entities.RedeemStateEntity
import app.eluvio.wallet.network.dto.NftRedeemableOfferDto
import app.eluvio.wallet.util.realm.toRealmInstant

fun NftRedeemableOfferDto.toEntity(): RedeemStateEntity {
    val dto = this
    return RedeemStateEntity().apply {
        offerId = dto.id
        active = dto.active
        redeemer = dto.redeemer
        redeemed = dto.redeemed?.toRealmInstant()
        transaction = dto.transaction
    }
}
