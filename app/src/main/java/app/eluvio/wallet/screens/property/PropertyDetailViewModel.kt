package app.eluvio.wallet.screens.property

import androidx.compose.ui.text.AnnotatedString
import androidx.core.text.HtmlCompat
import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.entities.v2.MediaPageEntity
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity.SectionItemEntity.Companion.MEDIA_CONTAINERS
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.screens.destinations.PropertyDetailDestination
import app.eluvio.wallet.util.rx.mapNotNull
import app.eluvio.wallet.util.toAnnotatedString
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class PropertyDetailViewModel @Inject constructor(
    private val propertyStore: MediaPropertyStore,
    private val apiProvider: ApiProvider,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<DynamicPageLayoutState>(DynamicPageLayoutState()) {

    private val propertyId = PropertyDetailDestination.argsFrom(savedStateHandle).propertyId

    override fun onResume() {
        super.onResume()

        apiProvider.getFabricEndpoint()
            .subscribeBy { updateState { copy(imagesBaseUrl = it) } }
            .addTo(disposables)

        propertyStore.observeMediaProperty(propertyId)
            .mapNotNull { property ->
                property.mainPage?.let { property to it }
            }
            .switchMap { (property, mainPage) ->
                propertyStore.observeSections(property, mainPage)
                    .map { sections -> sections.associateBy { section -> section.id } }
                    .map { sections -> Triple(property, mainPage, sections) }
            }
            .subscribe { (property, mainPage, sections) ->
                updateState {
                    copy(
                        backgroundImagePath = mainPage.backgroundImagePath,
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
        return mainPage.title?.let { DynamicPageLayoutState.Row.Title(AnnotatedString(it)) }
    }

    private fun description(mainPage: MediaPageEntity): DynamicPageLayoutState.Row? {
        return mainPage.description?.let { DynamicPageLayoutState.Row.Description(AnnotatedString(it)) }
    }

    private fun descriptionRichText(mainPage: MediaPageEntity): DynamicPageLayoutState.Row? {
        return mainPage.descriptionRichText
            // Only fallback to RichText if neither title nor description are present.
            ?.takeIf { mainPage.title == null && mainPage.description == null }
            ?.let {
                DynamicPageLayoutState.Row.Description(
                    HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY).toAnnotatedString()
                )
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
                DynamicPageLayoutState.Row.Carousel(
                    title = section.title,
                    subtitle = section.subtitle,
                    items = section.items
                        .flatMap { item ->
                            // TODO: Also expand media collections
                            if (item.expand && item.mediaType in MEDIA_CONTAINERS) {
                                item.media?.mediaListItems.orEmpty()
                            } else {
                                listOf(item.media)
                            }
                        }
                        .filterNotNull()
                        .map { DynamicPageLayoutState.CarouselItem.Media(it) }
                )
            }
    }
}
