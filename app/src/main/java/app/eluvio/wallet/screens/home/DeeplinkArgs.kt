package app.eluvio.wallet.screens.home

import app.eluvio.wallet.data.stores.DeeplinkStore

data class DeeplinkArgs(
    val marketplace: String?,
    val sku: String?,
    val jwt: String?,
    val entitlement: String?
) {
    fun toDeeplinkRequest(): DeeplinkStore.DeeplinkRequest? {
        return DeeplinkStore.DeeplinkRequest(
            marketplace ?: return null,
            sku ?: return null,
            jwt = jwt,
            entitlement = entitlement
        )
    }
}
