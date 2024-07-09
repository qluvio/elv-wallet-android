package app.eluvio.wallet.data.stores

import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.network.api.mwv2.SearchApi
import app.eluvio.wallet.network.dto.v2.SearchFiltersDto
import app.eluvio.wallet.network.dto.v2.SearchResultsDto
import io.reactivex.rxjava3.core.Single
import io.realm.kotlin.Realm
import javax.inject.Inject

class PropertySearchStore @Inject constructor(
    private val apiProvider: ApiProvider,
    private val realm: Realm,
) {

    fun getFilters(propertyId: String, forceRefresh: Boolean = false): Single<SearchFiltersDto> {
        return apiProvider.getApi(SearchApi::class)
            .flatMap { it.getSearchFilters(propertyId) }
//            .map { TODO() }
    }

    fun search(query: String): Single<List<SearchResultsDto>> {
        return apiProvider.getApi(SearchApi::class)
            .flatMap { it.search(query) }
            .map { it.contents ?: emptyList() }
    }
}
