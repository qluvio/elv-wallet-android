package app.eluvio.wallet.data.stores

import app.eluvio.wallet.data.entities.v2.MediaPageEntity
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
import app.eluvio.wallet.data.entities.v2.MediaPropertyEntity
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.network.api.mwv2.MediaWalletV2Api
import app.eluvio.wallet.network.converters.v2.toEntity
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.realm.asFlowable
import app.eluvio.wallet.util.realm.saveTo
import app.eluvio.wallet.util.rx.mapNotNull
import app.eluvio.wallet.util.rx.zipWithGenerator
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.kotlin.zipWith
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import javax.inject.Inject

class MediaPropertyStore @Inject constructor(
    private val apiProvider: ApiProvider,
    private val realm: Realm,
) {

    fun observeMediaProperties(forceRefresh: Boolean = true): Flowable<List<MediaPropertyEntity>> {
        return realm.query<MediaPropertyEntity>()
            .asFlowable()
            .mergeWith(
                if (forceRefresh) {
                    // There's an assumption here the Properties will never be empty,
                    // or we'll get an infinite spinner.
                    // See [ContentStore] for a more robust implementation.
                    // But it's safe to assume properties will never be empty because we're getting
                    // something even if we don't own anything yet.
                    fetchMediaProperties()
                } else {
                    Completable.complete()
                }
            )
            .switchMap {
                // In theory can be an infinite network request loop, but assuming server will never return empty
                if (it.isEmpty()) {
                    fetchMediaProperties().andThen(Flowable.just(it))
                } else {
                    Flowable.just(it)
                }
            }
    }

    private fun fetchMediaProperties(): Completable {
        return apiProvider.getApi(MediaWalletV2Api::class)
            .flatMap { api -> api.getProperties() }
            .doOnError {
                Log.e("Error fetching properties: $it")
            }
            .retry(
                // Would be nice to add a delay between retries, but that's a bit more complex.
                // Maybe some day.
                3
            )
            .map { response ->
                response.contents.orEmpty().map { propertyDto -> propertyDto.toEntity() }
            }
            .saveTo(realm)
            .ignoreElement()
    }

    fun observeMediaProperty(
        propertyId: String,
        forceRefresh: Boolean = true
    ): Flowable<MediaPropertyEntity> {
        return realm.query<MediaPropertyEntity>(
            "${MediaPropertyEntity::id.name} == $0",
            propertyId
        )
            .asFlowable()
            // Force refresh on first DB emit only
            .zipWithGenerator(forceRefresh) { false }
            .switchMap { (properties, refresh) ->
                val cachedProperty = properties.firstOrNull()
                when {
                    cachedProperty == null -> {
                        // Won't actually emit anything, we're just waiting for it to complete and
                        // save to DB.
                        Log.v("No cached property for id $propertyId, fetching from network.")
                        fetchMediaProperty(propertyId).toFlowable()
                    }

                    refresh -> {
                        Log.v("Force refreshing property for id $propertyId.")
                        Flowable.just(cachedProperty).mergeWith(fetchMediaProperty(propertyId))
                    }

                    else -> {
                        // Just emit the cached property
                        Flowable.just(cachedProperty)
                    }
                }
            }
    }

    fun observeSections(
        property: MediaPropertyEntity,
        page: MediaPageEntity,
        forceRefresh: Boolean = true
    ): Flowable<List<MediaPageSectionEntity>> {
        return realm.query<MediaPageSectionEntity>(
            "${MediaPageSectionEntity::id.name} IN $0",
            page.sectionIds
        )
            .asFlowable()
            // Whenever the DB emits, this will combine with the [forceRefresh] value for the
            // first item, but [false] for the rest. That way we can hit the network only once,
            // rather than every time the DB emits.
            .zipWithGenerator(forceRefresh) { false }
            .distinctUntilChanged(
                // This avoids an infinite loop when we can't fetch all sections in one page,
                // because until we implement pagination, there will always be missing sections.
            )
            .switchMap { (sections, refreshAll) ->
                val existingSections = if (refreshAll) {
                    emptySet()
                } else {
                    sections.map { it.id }.toSet()
                }
                Log.w("Existing sections (will NOT be fetched): $existingSections")
                fetchMissingSections(property.id, page, existingSections)
                    .startWith(Flowable.just(sections))
            }
    }

    fun observeSection(sectionId: String): Flowable<MediaPageSectionEntity> {
        return realm.query<MediaPageSectionEntity>(
            "${MediaPageSectionEntity::id.name} == $0",
            sectionId
        )
            .asFlowable()
            .mapNotNull { it.firstOrNull() }
    }

    private fun fetchMediaProperty(propertyId: String): Completable {
        return apiProvider.getApi(MediaWalletV2Api::class)
            .flatMap { api -> api.getProperty(propertyId) }
            .doOnError { Log.e("Error fetching property: $it") }
            .map { response -> response.toEntity() }
            .saveTo(realm)
            .ignoreElement()
    }

    private fun fetchMissingSections(
        propertyId: String,
        page: MediaPageEntity,
        existingSections: Set<String>
    ): Completable {
        val missingSections = page.sectionIds.subtract(existingSections)
        return if (missingSections.isEmpty()) {
            Completable.complete()
                .doOnSubscribe {
                    Log.d("All sections are already in the database. Nothing to fetch.")
                }
        } else {
            apiProvider.getApi(MediaWalletV2Api::class)
                .doOnSubscribe { Log.d("Fetching missing sections: $missingSections") }
                .flatMap { api -> api.getSectionsById(propertyId, missingSections.toList()) }
                .doOnError { Log.e("Error fetching sections", it) }
                .zipWith(apiProvider.getFabricEndpoint())
                .map { (response, baseUrl) ->
                    response.contents.orEmpty().map { sectionDto -> sectionDto.toEntity(baseUrl) }
                }
                .saveTo(realm, clearTable = false)
                .ignoreElement()
        }
    }
}
