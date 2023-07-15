package app.eluvio.wallet.util.realm

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList

/**
 * Same as [toRealmList], but converts [null] lists to an empty RealmList.
 */
fun <T> List<T>?.toRealmListOrEmpty(): RealmList<T> = this?.toRealmList() ?: realmListOf()
