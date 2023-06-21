package app.eluvio.wallet.util.realm

import app.eluvio.wallet.data.entities.CompositeKeyEntity
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
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

fun <T : RealmObject> Single<T>.saveTo(
    realm: Realm,
    updatePolicy: UpdatePolicy = UpdatePolicy.ALL
): Single<T> {
    return doOnSuccess { entity ->
        saveBlocking(realm, listOf(entity), updatePolicy)
    }
}

@JvmName("saveListTo") // prevent clash with non-list version
fun <T : RealmObject> Single<List<T>>.saveTo(
    realm: Realm,
    updatePolicy: UpdatePolicy = UpdatePolicy.ALL
): Single<List<T>> {
    return doOnSuccess { list ->
        saveBlocking(realm, list, updatePolicy)
    }
}

private fun saveBlocking(
    realm: Realm,
    list: List<RealmObject>,
    updatePolicy: UpdatePolicy = UpdatePolicy.ALL
) {
    realm.writeBlocking {
        list.forEach { entity ->
            (entity as? CompositeKeyEntity)?.updateKey()
            copyToRealm(entity, updatePolicy)
        }
    }
}
