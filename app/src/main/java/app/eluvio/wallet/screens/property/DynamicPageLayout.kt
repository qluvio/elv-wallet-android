package app.eluvio.wallet.screens.property

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.RedeemableOfferEntity
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.screens.common.DelayedFullscreenLoader
import app.eluvio.wallet.screens.property.rows.BannerSection
import app.eluvio.wallet.screens.property.rows.CarouselSection
import app.eluvio.wallet.screens.property.rows.DescriptionSection
import app.eluvio.wallet.screens.property.rows.TitleSection
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.util.compose.icons.Eluvio
import app.eluvio.wallet.util.compose.icons.Search
import app.eluvio.wallet.util.logging.Log
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun DynamicPageLayout(state: DynamicPageLayoutState) {
    if (state.isEmpty()) {
        DelayedFullscreenLoader()
        return
    }
    if (state.backgroundImagePath != null) {
        val url = state.urlForPath(state.backgroundImagePath)
        AsyncImage(
            model = url,
            contentScale = ContentScale.FillWidth,
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize()
        )
    }
    val listFocusRequester = remember { FocusRequester() }
    val scrollState = rememberLazyListState()
    if (state.searchNavigationEvent != null) {
        SearchButton(
            searchNavigationEvent = state.searchNavigationEvent,
            scrollState,
            listFocusRequester,
        )
    }
    LazyColumn(
        state = scrollState,
        contentPadding = PaddingValues(top = 66.dp, bottom = 32.dp),
        modifier = Modifier.focusRequester(listFocusRequester)
    ) {
        sections(state.sections, state.imagesBaseUrl)
    }
}

/**
 * Renders the [sections] inside a [LazyColumn].
 */
fun LazyListScope.sections(
    sections: List<DynamicPageLayoutState.Section>,
    imagesBaseUrl: String?
) {
    sections.forEach { section ->
        item(contentType = section::class) {
            when (section) {
                is DynamicPageLayoutState.Section.Banner -> BannerSection(
                    item = section,
                    imagesBaseUrl
                )

                is DynamicPageLayoutState.Section.Carousel -> CarouselSection(
                    item = section,
                    imagesBaseUrl
                )

                is DynamicPageLayoutState.Section.Description -> DescriptionSection(item = section)
                is DynamicPageLayoutState.Section.Title -> TitleSection(item = section)
            }
        }
    }
}

@Composable
private fun SearchButton(
    searchNavigationEvent: NavigationEvent,
    listScrollState: LazyListState,
    listFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    var firstFocus by rememberSaveable { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val offset by remember(listScrollState) {
        derivedStateOf {
            if (listScrollState.firstVisibleItemIndex == 0) {
                listScrollState.firstVisibleItemScrollOffset
            } else {
                // This assumes that the first item in the list is tall enough to make the search
                // button completely scroll off the screen
                Int.MAX_VALUE
            }
        }
    }
    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier.fillMaxWidth(),
    ) {
        val navigator = LocalNavigator.current
        Surface(
            onClick = { navigator(searchNavigationEvent) },
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            modifier = modifier
                .offset { IntOffset(0, -offset) }
                .focusProperties { down = listFocusRequester }
                .onFocusChanged {
                    if (it.hasFocus) {
                        if (firstFocus) {
                            // Prevent Search button from getting focus before other page elements
                            Log.d("Skipping Search button focus, moving down.")
                            firstFocus = false
                            listFocusRequester.requestFocus()
                        } else {
                            // When search button gains focus, make sure list is scrolled to the top
                            scope.launch {
                                listScrollState.animateScrollToItem(0)
                            }
                        }
                    }
                }
                .padding(top = 37.dp, end = 47.dp)
                .size(30.dp),
        ) {
            Icon(
                imageVector = Icons.Eluvio.Search,
                contentDescription = "Search",
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2d2d2d))
                    .padding(7.dp)
            )
        }
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun DynamicPageLayoutPreview() = EluvioThemePreview {
    DynamicPageLayout(
        DynamicPageLayoutState(
            searchNavigationEvent = NavigationEvent.GoBack,
            sections = listOf(
                DynamicPageLayoutState.Section.Title("1", AnnotatedString("Title")),
                DynamicPageLayoutState.Section.Banner("2", "https://foo.com/image.jpg"),
                DynamicPageLayoutState.Section.Description("3", AnnotatedString("Description")),
                DynamicPageLayoutState.Section.Carousel(
                    sectionId = "4",
                    title = "Carousel",
                    subtitle = "Subtitle",
                    items = listOf(
                        DynamicPageLayoutState.CarouselItem.Media(
                            entity = MediaEntity().apply {
                                id = "1"
                                name = "Media 1"
                                mediaType = "image"
                            },
                            propertyId = "property1"
                        ),
                        DynamicPageLayoutState.CarouselItem.RedeemableOffer(
                            offerId = "1",
                            name = "Offer 1",
                            fulfillmentState = RedeemableOfferEntity.FulfillmentState.AVAILABLE,
                            contractAddress = "0x123",
                            tokenId = "1",
                            imageUrl = "https://via.placeholder.com/150",
                            animation = null
                        )
                    )
                )
            )
        )
    )
}
