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
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.kotlin.zipWith
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import javax.inject.Inject

class MediaPropertyStore @Inject constructor(
    private val apiProvider: ApiProvider,
    private val tokenStore: TokenStore,
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
        if (!tokenStore.isLoggedIn) {
            // Since getProperties is a public API, this can be called right after SignOut.
            // It's not a huge deal because it only "leaks" public data, but it still causes
            // problems when switching between Main and Demo envs, so adding this safety check here.
            // Also worth noting, that if we ever bring back a no-auth experience, we'll have to
            // fix this a different way.
            return Completable.complete()
        }
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
        return observeRealmAndFetch(
            realmQuery = realm.query<MediaPropertyEntity>(
                "${MediaPropertyEntity::id.name} == $0",
                propertyId
            )
                .asFlowable(),
            fetchOperation = { properties, isFirstState ->
                fetchMediaProperty(propertyId)
                    .doOnSubscribe { Log.d("Fetching MediaProperty: $propertyId") }
                    .takeIf { properties.isEmpty() || (isFirstState && forceRefresh) }
            }
        )
            .mapNotNull { it.firstOrNull() }
    }

    fun observeSections(
        property: MediaPropertyEntity,
        page: MediaPageEntity,
        forceRefresh: Boolean = true
    ): Flowable<List<MediaPageSectionEntity>> {
        return observeRealmAndFetch(
            realmQuery = realm.query<MediaPageSectionEntity>(
                "${MediaPageSectionEntity::id.name} IN $0",
                page.sectionIds
            ).asFlowable(),
            fetchOperation = { sections, isFirst ->
                val existingSections = if (forceRefresh && isFirst) {
                    emptySet()
                } else {
                    sections.map { it.id }.toSet()
                }
                Log.w("Existing sections (will NOT be fetched): $existingSections")
                fetchMissingSections(property.id, page, existingSections)
            }
        )
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
