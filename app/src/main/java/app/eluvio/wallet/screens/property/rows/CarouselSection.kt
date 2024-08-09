package app.eluvio.wallet.screens.property.rows

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.screens.common.Overscan
import app.eluvio.wallet.screens.common.spacer
import app.eluvio.wallet.screens.property.DynamicPageLayoutState
import app.eluvio.wallet.screens.property.DynamicPageLayoutState.CarouselItem
import app.eluvio.wallet.screens.property.items.CarouselItemCard
import app.eluvio.wallet.theme.body_32
import app.eluvio.wallet.theme.carousel_36
import app.eluvio.wallet.theme.label_24
import app.eluvio.wallet.util.compose.focusTrap
import app.eluvio.wallet.util.compose.thenIfNotNull
import coil.compose.AsyncImage

private val CARD_HEIGHT = 120.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CarouselSection(item: DynamicPageLayoutState.Section.Carousel, imagesBaseUrl: String?) {
    if (item.items.isEmpty()) {
        return
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .thenIfNotNull(item.backgroundColor) { Modifier.background(it) }
    ) {
        if (item.backgroundImagePath != null) {
            AsyncImage(
                model = "$imagesBaseUrl${item.backgroundImagePath}",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopStart,
                modifier = Modifier.matchParentSize()
            )
        }
        Row {
            Logo(item, imagesBaseUrl)
            Column(
                Modifier
                    .focusTrap(FocusDirection.Left, FocusDirection.Right)
                    .focusGroup() // Required to make focusRestorer() work down the line
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Whether or not to add padding to the start of the row.
                val startPadding = if (item.logoPath == null) Overscan.horizontalPadding else 30.dp

                var selectedFilter by remember { mutableStateOf<AttributeAndValue?>(null) }
                val filterRowFocusRequester = remember { FocusRequester() }

                val title = item.title?.takeIf { it.isNotEmpty() }
                val subtitle = item.subtitle?.takeIf { it.isNotEmpty() }
                val hasTitleRow = title != null || subtitle != null
                if (hasTitleRow) {
                    TitleRow(
                        title,
                        subtitle,
                        item.viewAllNavigationEvent,
                        imagesBaseUrl,
                        startPadding
                    )
                }
                if (item.filterAttribute != null) {
                    FilterSelectorRow(
                        selectedValue = selectedFilter?.second,
                        attributeValues = item.filterAttribute.values.map { it.value },
                        onValueSelected = { tag ->
                            selectedFilter = tag?.let { item.filterAttribute.id to it }
                        },
                        modifier = Modifier
                            .focusRequester(filterRowFocusRequester)
                            .padding(top = 8.dp),
                        item.viewAllNavigationEvent?.takeIf { !hasTitleRow },
                        startPadding = startPadding
                    )
                } else if (!hasTitleRow && item.viewAllNavigationEvent != null) {
                    ViewAllButton(
                        item.viewAllNavigationEvent,
                        modifier = Modifier.padding(start = Overscan.horizontalPadding)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val exitFocusModifier = Modifier
                    .focusProperties {
                        exit = {
                            if (it == FocusDirection.Up && item.filterAttribute != null) {
                                // Prevent focus from skipping over the filter row
                                filterRowFocusRequester
                            } else {
                                FocusRequester.Default
                            }
                        }
                    }
                    .focusGroup() // Required to make focusRestorer() work down the line
                val filteredItems = rememberFilteredItems(item.items, selectedFilter)
                if (selectedFilter != null && filteredItems.isEmpty()) {
                    // This shouldn't happen on a properly configured tenant, but just in case,
                    // we want to make sure the row height stays relatively consistent.
                    Text(
                        text = "Nothing here... yet?",
                        modifier = Modifier
                            .padding(horizontal = Overscan.horizontalPadding)
                            .height(CARD_HEIGHT)
                            .wrapContentHeight(Alignment.CenterVertically)
                    )
                } else if (item.showAsGrid) {
                    ItemGrid(filteredItems, startPadding, modifier = exitFocusModifier)
                } else {
                    ItemRow(filteredItems, startPadding, modifier = exitFocusModifier)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun Logo(
    item: DynamicPageLayoutState.Section.Carousel,
    imagesBaseUrl: String?,
) {
    item.logoPath ?: return
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            // This doesn't respect overscan, and will start slightly before it.
            // Go argue with the design team.
            .padding(start = 30.dp, top = 40.dp)
            .width(95.dp)
    ) {
        AsyncImage(
            model = "$imagesBaseUrl${item.logoPath}",
            contentDescription = "Logo"
        )
        if (item.logoText != null) {
            Text(
                item.logoText,
                style = MaterialTheme.typography.label_24,
                modifier = Modifier.padding(vertical = 20.dp)
            )
        }
    }
}

@Composable
private fun ColumnScope.TitleRow(
    title: String?,
    subtitle: String?,
    viewAllNavigationEvent: NavigationEvent?,
    imagesBaseUrl: String?,
    startPadding: Dp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .padding(start = startPadding, end = Overscan.horizontalPadding)
    ) {
        title?.let {
            Text(
                it,
                style = MaterialTheme.typography.body_32,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))

        viewAllNavigationEvent?.let {
            ViewAllButton(it)
        }
    }

    subtitle?.let {
        Text(
            it,
            style = MaterialTheme.typography.body_32,
            fontWeight = FontWeight.Light,
            modifier = Modifier
                .padding(start = startPadding, end = Overscan.horizontalPadding, top = 4.dp)
        )
    }
}

@Composable
private fun ViewAllButton(
    navigationEvent: NavigationEvent,
    modifier: Modifier = Modifier,
) {
    val navigator = LocalNavigator.current
    Surface(
        onClick = { navigator(navigationEvent) },
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(BorderStroke(1.dp, Color.White)),
            focusedBorder = Border.None
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(3.dp)),
        modifier = modifier
    ) {
        Text(
            "VIEW ALL",
            style = MaterialTheme.typography.body_32,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp, vertical = 5.dp)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
private fun ItemGrid(items: List<CarouselItem>, startPadding: Dp, modifier: Modifier = Modifier) {
    // TODO: make lazy
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .focusRestorer()
            .padding(start = startPadding, end = Overscan.horizontalPadding)
    ) {
        items.forEach { item ->
            key(item) {
                CarouselItemCard(
                    carouselItem = item,
                    cardHeight = CARD_HEIGHT,
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ItemRow(items: List<CarouselItem>, startPadding: Dp, modifier: Modifier = Modifier) {
    // The 'key' function prevents from focusRestorer() from breaking when crashing when
    // filteredItems changes.
    // From what I could tell it's kind of like 'remember' but for Composable.
    key(items) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(start = startPadding, end = Overscan.horizontalPadding),
            modifier = modifier.focusRestorer()
        ) {
            items(items) { item ->
                CarouselItemCard(
                    carouselItem = item,
                    cardHeight = CARD_HEIGHT,
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FilterSelectorRow(
    selectedValue: String?,
    attributeValues: List<String>,
    onValueSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    viewAllNavigationEvent: NavigationEvent?,
    startPadding: Dp
) {
    val firstItemFocusRequester = remember { FocusRequester() }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(start = startPadding, end = Overscan.horizontalPadding),
        modifier = modifier.focusRestorer { firstItemFocusRequester },
    ) {
        // TODO: there might not always be an "All" option (e.g. Season 1/2/3 etc)
        item {
            FilterTab(
                text = "All",
                value = null,
                onSelected = onValueSelected,
                selected = selectedValue == null,
                modifier = Modifier.focusRequester(firstItemFocusRequester)
            )
        }
        items(attributeValues) { attributeValue ->
            FilterTab(
                text = attributeValue,
                selected = selectedValue == attributeValue,
                onSelected = onValueSelected
            )
        }

        viewAllNavigationEvent?.let {
            item { ViewAllButton(it) }
        }
        spacer(width = 20.dp)
    }
}

@Composable
private fun FilterTab(
    text: String,
    selected: Boolean,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    value: String? = text,
) {
    Surface(
        onClick = { onSelected(value) },
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = if (selected) Color.White else Color(0xFF7B7B7B),
            pressedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
        ),
        modifier = modifier
            .onFocusChanged {
                if (it.hasFocus) {
                    onSelected(value)
                }
            }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.body_32,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun rememberFilteredItems(
    items: List<CarouselItem>,
    selectedFilter: Pair<String, String>?
): List<CarouselItem> {
    return remember(items, selectedFilter) {
        if (selectedFilter == null) {
            items
        } else {
            items
                .filterIsInstance<CarouselItem.Media>()
                .filter {
                    it.entity.attributes
                        .firstOrNull { attribute -> attribute.id == selectedFilter.first }
                        ?.values
                        ?.map { tag -> tag.value }
                        ?.contains(selectedFilter.second) == true
                }
        }
    }
}

typealias AttributeAndValue = Pair<String, String>
