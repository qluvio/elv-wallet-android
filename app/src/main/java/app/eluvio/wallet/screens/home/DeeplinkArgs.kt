package app.eluvio.wallet.screens.home

import app.eluvio.wallet.data.entities.deeplink.DeeplinkRequestEntity

data class DeeplinkArgs(
    val action: String?,
    val marketplace: String?,
    val contract: String?,
    val sku: String?,
    val jwt: String?,
    val entitlement: String?,
    val backLink: String?
) {
    fun toDeeplinkRequest(): DeeplinkRequestEntity? {
        val entity = this
        return DeeplinkRequestEntity().apply {
            this.action = entity.action ?: return null
            this.marketplace = entity.marketplace ?: return null
            this.contract = entity.contract ?: return null
            this.sku = entity.sku ?: return null
            this.jwt = entity.jwt
            this.entitlement = entity.entitlement
            this.backLink = entity.backLink
        }
    }
}
