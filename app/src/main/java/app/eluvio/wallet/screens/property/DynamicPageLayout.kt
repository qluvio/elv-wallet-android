package app.eluvio.wallet.screens.property

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyListItemScope
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.RedeemableOfferEntity
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.screens.common.DelayedFullscreenLoader
import app.eluvio.wallet.screens.common.spacer
import app.eluvio.wallet.screens.property.rows.BannerRow
import app.eluvio.wallet.screens.property.rows.CarouselRow
import app.eluvio.wallet.screens.property.rows.DescriptionRow
import app.eluvio.wallet.screens.property.rows.TitleRow
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.util.logging.Log
import coil.compose.AsyncImage

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
    // Used to prevent Search button from getting focus before other page elements
    TvLazyColumn(pivotOffsets = PivotOffsets(0.6f)) {
        item(contentType = "search", key = "search") {
            if (state.searchNavigationEvent != null) {
                var firstFocus by remember { mutableStateOf(true) }
                val focusManager = LocalFocusManager.current
                SearchButton(
                    searchNavigationEvent = state.searchNavigationEvent,
                    modifier = Modifier.onFocusChanged {
                        if (it.isFocused && firstFocus) {
                            Log.d("Skipping Search button focus, moving down.")
                            firstFocus = false
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    })
            } else {
                // Empty item to make top of list focusable
                Spacer(
                    Modifier
                        .height(32.dp)
                        .focusable()
                )
            }
        }
        state.rows.forEach { row ->
            item(contentType = row::class) {
                when (row) {
                    is DynamicPageLayoutState.Row.Banner -> BannerRow(
                        item = row,
                        state
                    )

                    is DynamicPageLayoutState.Row.Carousel -> CarouselRow(item = row)
                    is DynamicPageLayoutState.Row.Description -> DescriptionRow(item = row)
                    is DynamicPageLayoutState.Row.Title -> TitleRow(item = row)
                }
            }
        }

        spacer(height = 32.dp)
    }
}

@Composable
private fun TvLazyListItemScope.SearchButton(
    searchNavigationEvent: NavigationEvent,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier.fillParentMaxWidth(),
    ) {
        val navigator = LocalNavigator.current
        Surface(
            onClick = { navigator(searchNavigationEvent) },
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            modifier = modifier
                .padding(8.dp)
                .size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
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
            rows = listOf(
                DynamicPageLayoutState.Row.Title(AnnotatedString("Title")),
                DynamicPageLayoutState.Row.Banner("https://foo.com/image.jpg"),
                DynamicPageLayoutState.Row.Description(AnnotatedString("Description")),
                DynamicPageLayoutState.Row.Carousel(
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
