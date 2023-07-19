package app.eluvio.wallet.network.converters

import app.eluvio.wallet.data.entities.RedeemStateEntity
import app.eluvio.wallet.network.api.authd.RedeemableOffersApi
import app.eluvio.wallet.network.dto.NftInfoDto
import app.eluvio.wallet.network.dto.NftRedeemableOfferDto
import app.eluvio.wallet.network.dto.RedemptionStatusDto
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.realm.toRealmInstant
import io.realm.kotlin.types.RealmInstant

fun NftInfoDto.toRedeemStateEntities(statuses: List<RedemptionStatusDto>): List<RedeemStateEntity> {
    val contract = contract_addr.removePrefix("0x")
    val relevantStatuses = statuses.filter {
        it.operation == RedeemableOffersApi.REDEEM_OPERATION &&
                it.contract == contract &&
                it.tokenId == token_id_str
    }
    return offers?.map { offer -> offer.toEntity(relevantStatuses) } ?: emptyList()
}

/**
 *[statuses] should be pre-filtered to only include relevant statuses
 */
private fun NftRedeemableOfferDto.toEntity(statuses: List<RedemptionStatusDto>): RedeemStateEntity {
    val dto = this
    val statusDto = statuses.firstOrNull { it.offerId == dto.id }

    return RedeemStateEntity().apply {
        offerId = dto.id
        active = dto.active
        redeemer = dto.redeemer
        redeemed = dto.redeemed?.toRealmInstant()
        transaction = dto.transaction
        status = parseRedemptionStatus(redeemed, statusDto)
    }
}

private fun parseRedemptionStatus(
    redeemedDate: RealmInstant?,
    statusDto: RedemptionStatusDto?
): RedeemStateEntity.Status {
    val statusStr = statusDto?.status
    return when {
        redeemedDate != null -> RedeemStateEntity.Status.REDEEMED
        statusDto == null -> {
            // Unless redemption has occurred in the last 24h, it won't be found in the statuses.
            // This is normal and expected for most Offers.
            RedeemStateEntity.Status.UNREDEEMED
        }

        statusStr == "complete" -> RedeemStateEntity.Status.REDEEMED
        statusStr == "failed" -> RedeemStateEntity.Status.REDEEM_FAILED
        statusStr == "" || statusStr == null -> {
            // The BE sends "" to signal that the redemption is in progress. Don't ask me why.
            // catching nulls just in case the BE starts omitting "" from its responses, resulting in [null] in our DTO.
            RedeemStateEntity.Status.REDEEMING
        }

        else -> {
            // There *is* a redemption status, but it's not one we recognize.
            Log.w("Unknown redemption status: ${statusDto.status}. Defaulting to UNREDEEMED")
            RedeemStateEntity.Status.REDEEMING
        }
    }
}
