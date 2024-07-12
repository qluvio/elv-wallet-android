package app.eluvio.wallet.screens.property

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyListItemScope
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
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
import app.eluvio.wallet.theme.LocalSurfaceScale
import app.eluvio.wallet.theme.label_24
import app.eluvio.wallet.util.findActivity
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
    val lazyColumnFocusRequester = remember { FocusRequester() }
    val backButtonFocusRequester = remember { FocusRequester() }
    if (state.backLinkUrl != null) {
        BackToThirdPartyButton(
            state.backButtonLogo,
            Modifier
                .focusRequester(backButtonFocusRequester)
                .focusProperties { down = lazyColumnFocusRequester }
        )
    }
    TvLazyColumn(
        pivotOffsets = PivotOffsets(0.6f),
        modifier = Modifier
            .focusRequester(lazyColumnFocusRequester)
            .focusProperties { up = backButtonFocusRequester })
    {
        item {
            if (state.searchNavigationEvent != null) {
                SearchButton(searchNavigationEvent = state.searchNavigationEvent)
            } else {
                // Empty item to make top of list focusable
                Spacer(Modifier.height(32.dp).focusable())
            }
        }
        state.rows.forEach { row ->
            item {
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
private fun TvLazyListItemScope.SearchButton(searchNavigationEvent: NavigationEvent) {
    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier.fillParentMaxWidth(),
    ) {
        val navigator = LocalNavigator.current
        Surface(
            onClick = { navigator(searchNavigationEvent) },
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            modifier = Modifier.padding(8.dp).size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier.fillMaxSize().padding(6.dp)
            )
        }
    }
}

@Composable
private fun BackToThirdPartyButton(
    backButtonLogo: String?,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 32.dp, end = 32.dp)
    ) {
        val border = Border(
            BorderStroke(2.dp, MaterialTheme.colorScheme.onSecondaryContainer),
            shape = MaterialTheme.shapes.extraSmall
        )
        val context = LocalContext.current
        Surface(
            onClick = { context.findActivity()?.finish() },
            border = ClickableSurfaceDefaults.border(border = border),
            scale = LocalSurfaceScale.current,
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.Black.copy(alpha = 0.2f),
                focusedContainerColor = Color.Black
            ),
            modifier = Modifier.size(width = 160.dp, height = 40.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // TODO: handle logo missing / error loading
                val text = if (backButtonLogo != null) "Back to" else "Go Back"
                Text(
                    text,
                    style = MaterialTheme.typography.label_24.copy(fontSize = 16.sp),
                )
                backButtonLogo?.let { logo ->
                    // scale logo request from server to save bandwidth
                    val url = logo + "?h=" + LocalDensity.current.run { 20.dp.roundToPx() }
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        placeholder = rememberVectorPainter(
                            defaultWidth = 50.dp,
                            defaultHeight = 20.dp,
                            autoMirror = false,
                        ) { _, _ ->
                            // Empty vector painter to take up the space we expect the logo to be while we load it
                        },
                        modifier = Modifier
                            .height(20.dp)
                            .padding(start = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun DynamicPageLayoutPreview() = EluvioThemePreview {
    DynamicPageLayout(DynamicPageLayoutState(
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
    ))
}
