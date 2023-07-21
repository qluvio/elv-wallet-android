package app.eluvio.wallet.util.realm

import app.eluvio.wallet.util.logging.Log
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.delete
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.rx3.rxFlowable

// This is very simplified and doesn't handle incremental updates optimally
fun <T : RealmObject> RealmQuery<T>.asFlowable(): Flowable<List<T>> {
    return rxFlowable<List<T>> {
        asFlow().collect { trySend(it.list) }
    }
        .subscribeOn(Schedulers.io())
}

inline fun <reified T : RealmObject> Single<T>.saveTo(
    realm: Realm,
    clearTable: Boolean = false,
    updatePolicy: UpdatePolicy = UpdatePolicy.ALL
): Single<T> {
    return doOnSuccess { entity ->
        saveBlocking(realm, listOf(entity), clearTable, updatePolicy)
    }
}

/**
 * Saves a list of entities to the database.
 * @param clearTable If true, the table will be cleared before inserting the new entities.
 */
@JvmName("saveListTo") // prevent clash with non-list version
inline fun <reified T : RealmObject> Single<List<T>>.saveTo(
    realm: Realm,
    clearTable: Boolean = false,
    updatePolicy: UpdatePolicy = UpdatePolicy.ALL
): Single<List<T>> {
    return doOnSuccess { list ->
        saveBlocking(realm, list, clearTable, updatePolicy)
    }
}

inline fun <reified T : RealmObject> saveBlocking(
    realm: Realm,
    list: List<T>,
    clearTable: Boolean = false,
    updatePolicy: UpdatePolicy = UpdatePolicy.ALL
) {
    realm.writeBlocking {
        if (clearTable) {
            Log.w("Clearing table ${T::class.simpleName}")
            delete<T>()
        }
        list.forEach { entity ->
            copyToRealm(entity, updatePolicy)
        }
    }
}
