package app.eluvio.wallet.data.entities.v2.permissions

import app.eluvio.wallet.util.realm.RealmEnum

enum class PermissionBehavior(override val value: String) : RealmEnum {
    HIDE("hide"),
    DISABLE("disable"),
    SHOW_PURCHASE("show_purchase"),
    SHOW_ALTERNATE_PAGE("show_alternate_page"),
    ;

    companion object {
        fun fromValue(value: String): PermissionBehavior? {
            return entries.firstOrNull { it.value == value }
        }
    }
}
