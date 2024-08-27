package app.eluvio.wallet.data.entities.v2.permissions

val PermissionsEntity.isHidden: Boolean
    get() = authorized == false && behaviorEnum == PermissionBehavior.HIDE

val PermissionsEntity.isDisabled: Boolean
    get() = authorized == false && behaviorEnum == PermissionBehavior.DISABLE

val PermissionsEntity.showPurchaseOptions: Boolean
    get() = authorized == false && behaviorEnum == PermissionBehavior.SHOW_PURCHASE

val PermissionsEntity.showAlternatePage: Boolean
    get() = authorized == false && behaviorEnum == PermissionBehavior.SHOW_ALTERNATE_PAGE && alternatePageId != null

val PermissionsEntity.behaviorEnum: PermissionBehavior?
    get() = behavior?.let { PermissionBehavior.fromValue(it) }
