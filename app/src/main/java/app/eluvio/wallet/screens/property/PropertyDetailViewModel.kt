package app.eluvio.wallet.screens.property

import androidx.compose.ui.text.AnnotatedString
import androidx.core.text.parseAsHtml
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.v2.MediaPageEntity
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
import app.eluvio.wallet.data.entities.v2.SearchFiltersEntity
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.data.stores.PropertySearchStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.destinations.MediaGridDestination
import app.eluvio.wallet.screens.destinations.PropertyDetailDestination
import app.eluvio.wallet.screens.destinations.PropertySearchDestination
import app.eluvio.wallet.util.toAnnotatedString
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class PropertyDetailViewModel @Inject constructor(
    private val propertyStore: MediaPropertyStore,
    private val propertySearchStore: PropertySearchStore,
    private val apiProvider: ApiProvider,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<DynamicPageLayoutState>(DynamicPageLayoutState()) {

    private val propertyId = PropertyDetailDestination.argsFrom(savedStateHandle).propertyId

    override fun onResume() {
        super.onResume()

        apiProvider.getFabricEndpoint()
            .subscribeBy { updateState { copy(imagesBaseUrl = it) } }
            .addTo(disposables)

        val pageLayout = propertyStore.observeMediaProperty(propertyId)
            .switchMap { property ->
                val mainPage = property.mainPage ?: return@switchMap Flowable.empty()
                propertyStore.observeSections(property, mainPage)
                    .map { sections -> sections.associateBy { section -> section.id } }
                    .map { sections -> mainPage to sections }
            }
        val searchFilters = propertySearchStore.getFilters(propertyId)
            .onErrorReturnItem(SearchFiltersEntity())

        Flowable.combineLatest(pageLayout, searchFilters) { (page, sections), filters ->
            Triple(page, sections, filters)
        }
            .subscribe { (mainPage, sections, filters) ->
                updateState {
                    copy(
                        backgroundImagePath = mainPage.backgroundImagePath,
                        searchNavigationEvent = PropertySearchDestination(propertyId).asPush(),
                        sections = listOfNotNull(
                            logo(mainPage),
                            title(mainPage),
                            description(mainPage),
                            descriptionRichText(mainPage),
                        ) + sections(mainPage, sections, filters)
                    )
                }
            }
            .addTo(disposables)
    }

    private fun logo(mainPage: MediaPageEntity): DynamicPageLayoutState.Section? {
        return mainPage.logo?.let { DynamicPageLayoutState.Section.Banner(it) }
    }

    private fun title(mainPage: MediaPageEntity): DynamicPageLayoutState.Section? {
        return mainPage.title
            ?.takeIf { it.isNotEmpty() }
            ?.let { DynamicPageLayoutState.Section.Title(AnnotatedString(it)) }
    }

    private fun description(mainPage: MediaPageEntity): DynamicPageLayoutState.Section? {
        return mainPage.description
            ?.takeIf { it.isNotEmpty() }
            ?.let { DynamicPageLayoutState.Section.Description(AnnotatedString(it)) }
    }

    private fun descriptionRichText(mainPage: MediaPageEntity): DynamicPageLayoutState.Section? {
        return mainPage.descriptionRichText
            // Only fallback to RichText if neither title nor description are present.
            ?.takeIf { mainPage.title.isNullOrEmpty() && mainPage.description.isNullOrEmpty() }
            ?.let {
                DynamicPageLayoutState.Section.Description(it.parseAsHtml().toAnnotatedString())
            }
    }

    private fun sections(
        mainPage: MediaPageEntity,
        sections: Map<String, MediaPageSectionEntity>,
        filters: SearchFiltersEntity
    ): List<DynamicPageLayoutState.Section> {
        // We can't just iterate over [sections] because the order of sections is important and it
        // is defined by the Page's sectionIds.
        return mainPage.sectionIds.mapNotNull { sections[it] }
            .map { section -> section.toCarousel(propertyId, filters) }
    }
}
