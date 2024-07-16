package app.eluvio.wallet.screens.property

import androidx.compose.ui.text.AnnotatedString
import androidx.core.text.parseAsHtml
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.v2.MediaPageEntity
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
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

/**
 * If a section doesn't have a displayLimit, we still need to limit how many items the
 * client will load.
 */
private const val SECTION_DEFAULT_DISPLAY_LIMIT = 5

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

        // Prefetch search filters
//        propertySearchStore.getFilters(propertyId, forceRefresh = true)
//            .ignoreElement()
//            .subscribe()
//            .addTo(disposables)

        propertyStore.observeMediaProperty(propertyId)
            .switchMap { property ->
                val mainPage = property.mainPage ?: return@switchMap Flowable.empty()
                propertyStore.observeSections(property, mainPage)
                    .map { sections -> sections.associateBy { section -> section.id } }
                    .map { sections -> mainPage to sections }
            }
            .subscribe { (mainPage, sections) ->
                updateState {
                    copy(
                        backgroundImagePath = mainPage.backgroundImagePath,
                        searchNavigationEvent = PropertySearchDestination(propertyId).asPush(),
                        rows = listOfNotNull(
                            logo(mainPage),
                            title(mainPage),
                            description(mainPage),
                            descriptionRichText(mainPage),
                        ) + sections(mainPage, sections)
                    )
                }
            }
            .addTo(disposables)
    }

    private fun logo(mainPage: MediaPageEntity): DynamicPageLayoutState.Row? {
        return mainPage.logo?.let { DynamicPageLayoutState.Row.Banner(it) }
    }

    private fun title(mainPage: MediaPageEntity): DynamicPageLayoutState.Row? {
        return mainPage.title
            ?.takeIf { it.isNotEmpty() }
            ?.let { DynamicPageLayoutState.Row.Title(AnnotatedString(it)) }
    }

    private fun description(mainPage: MediaPageEntity): DynamicPageLayoutState.Row? {
        return mainPage.description
            ?.takeIf { it.isNotEmpty() }
            ?.let { DynamicPageLayoutState.Row.Description(AnnotatedString(it)) }
    }

    private fun descriptionRichText(mainPage: MediaPageEntity): DynamicPageLayoutState.Row? {
        return mainPage.descriptionRichText
            // Only fallback to RichText if neither title nor description are present.
            ?.takeIf { mainPage.title.isNullOrEmpty() && mainPage.description.isNullOrEmpty() }
            ?.let {
                DynamicPageLayoutState.Row.Description(it.parseAsHtml().toAnnotatedString())
            }
    }

    private fun sections(
        mainPage: MediaPageEntity,
        sections: Map<String, MediaPageSectionEntity>
    ): List<DynamicPageLayoutState.Row> {
        // We can't just iterate over [sections] because the order of sections is important and it
        // is defined by the Page's sectionIds.
        return mainPage.sectionIds.mapNotNull { sections[it] }
            .map { section ->
                val items = section.items.toCarouselItems(propertyId)
                DynamicPageLayoutState.Row.Carousel(
                    title = section.title,
                    subtitle = section.subtitle,
                    items = items,
                    showAllNavigationEvent = MediaGridDestination(
                        propertyId = propertyId,
                        sectionId = section.id
                    )
                        .takeIf {
                            items.size > (section.displayLimit ?: SECTION_DEFAULT_DISPLAY_LIMIT)
                        }
                        ?.asPush()
                )
            }
    }
}
