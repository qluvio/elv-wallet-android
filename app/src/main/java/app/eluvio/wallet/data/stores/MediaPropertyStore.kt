package app.eluvio.wallet.data.stores

import app.eluvio.wallet.data.entities.v2.MediaPageEntity
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
import app.eluvio.wallet.data.entities.v2.MediaPropertyEntity
import app.eluvio.wallet.data.entities.v2.OwnedPropertiesEntity
import app.eluvio.wallet.data.entities.v2.OwnedPropertiesRealmEntity
import app.eluvio.wallet.data.entities.v2.SectionItemEntity
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.network.api.mwv2.MediaWalletV2Api
import app.eluvio.wallet.network.converters.v2.toEntity
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.realm.asFlowable
import app.eluvio.wallet.util.realm.saveAsync
import app.eluvio.wallet.util.realm.saveTo
import app.eluvio.wallet.util.rx.mapNotNull
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.zipWith
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmDictionary
import javax.inject.Inject

class MediaPropertyStore @Inject constructor(
    private val apiProvider: ApiProvider,
    private val realm: Realm,
) {

    fun observeMediaProperties(forceRefresh: Boolean = true): Flowable<List<MediaPropertyEntity>> {
        val orderedProperties =
            Flowable.combineLatest(
                realm.query<MediaPropertyEntity>().asFlowable(),
                realm.query<MediaPropertyEntity.PropertyOrderEntity>().asFlowable()
                    .distinctUntilChanged()
                    .map { list -> list.associateBy { it.propertyId } }
            ) { properties, orderMap ->
                properties.sortedBy { orderMap[it.id]?.index ?: Int.MAX_VALUE }
            }

        return observeRealmAndFetch(
            realmQuery = orderedProperties,
            fetchOperation = { _, isFirstState ->
                fetchMediaProperties().takeIf { isFirstState && forceRefresh }
            }
        )
    }

    fun observeOwnedProperties(): Flowable<OwnedPropertiesEntity> {
        return observeRealmAndFetch(
            realmQuery = realm.query<OwnedPropertiesRealmEntity>().asFlowable(),
            fetchOperation = { _, isFirstState ->
                fetchOwnedProperties().takeIf { isFirstState }
            }
        )
            .mapNotNull { it.firstOrNull() }
    }

    private fun fetchOwnedProperties(): Completable {
        return apiProvider.getApi(MediaWalletV2Api::class)
            .flatMap { api -> api.getProperties(includePublic = false) }
            .map {
                OwnedPropertiesRealmEntity().apply {
                    properties = it.contents.orEmpty()
                        .associate { it.id to it.name }
                        .toRealmDictionary()
                }
            }
            .saveTo(realm)
            .ignoreElement()
    }

    fun fetchMediaProperties(): Completable {
        // TODO: find a way to not leak data between Demo and Main
        //        if (!tokenStore.isLoggedIn) {
        // Since getProperties is a public API, this can be called right after SignOut.
        // It's not a huge deal because it only "leaks" public data, but it still causes
        // problems when switching between Main and Demo envs, so adding this safety check here.
        // Also worth noting, that if we ever bring back a no-auth experience, we'll have to
        // fix this a different way.
        //            return Completable.complete()
        //        }
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
            .flatMapCompletable { response ->
                val properties =
                    response.contents.orEmpty().mapNotNull { propertyDto -> propertyDto.toEntity() }
                val order = properties
                    .mapIndexed { index, property ->
                        MediaPropertyEntity.PropertyOrderEntity().apply {
                            this.propertyId = property.id
                            // This will need to be updated once we support pagination
                            val page = 0
                            this.index = (page * 1000) + index
                        }
                    }

                Completable.mergeArray(
                    realm.saveAsync(properties),
                    realm.saveAsync(order)
                )
            }
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

    /**
     * Assumes the item is already cached, and doesn't fetch it from network.
     */
    fun getSectionItem(sectionItemId: String): Single<SectionItemEntity> {
        return realm.query<SectionItemEntity>(
            "${SectionItemEntity::id.name} == $0",
            sectionItemId
        )
            .asFlowable()
            .mapNotNull { it.firstOrNull() }
            .firstOrError()
    }

    private fun fetchMediaProperty(propertyId: String): Completable {
        return apiProvider.getApi(MediaWalletV2Api::class)
            .flatMap { api -> api.getProperty(propertyId) }
            .doOnError { Log.e("Error fetching property: $it") }
            .mapNotNull { response -> response.toEntity() }
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
