package app.eluvio.wallet.screens.property.rows

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.screens.common.Overscan
import app.eluvio.wallet.screens.common.TvButton
import app.eluvio.wallet.screens.common.spacer
import app.eluvio.wallet.screens.property.CarouselItemCard
import app.eluvio.wallet.screens.property.DynamicPageLayoutState
import app.eluvio.wallet.screens.property.DynamicPageLayoutState.CarouselItem
import app.eluvio.wallet.theme.body_32
import app.eluvio.wallet.theme.carousel_36
import app.eluvio.wallet.util.compose.focusTrap

private val CARD_HEIGHT = 170.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CarouselSection(item: DynamicPageLayoutState.Section.Carousel) {
    if (item.items.isEmpty()) {
        return
    }
    Spacer(modifier = Modifier.height(16.dp))
    Column(
        Modifier
            .focusTrap(FocusDirection.Left, FocusDirection.Right)
            .focusGroup() // Required to make focusRestorer() work down the line
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Overscan.horizontalPadding)
        ) {
            item.title?.takeIf { it.isNotEmpty() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.carousel_36,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            item.viewAllNavigationEvent?.let {
                val navigator = LocalNavigator.current
                TvButton(
                    text = "View All",
                    textStyle = MaterialTheme.typography.body_32,
                    onClick = { navigator(it) },
                )
            }
        }
        item.subtitle?.takeIf { it.isNotEmpty() }?.let {
            Text(
                it,
                style = MaterialTheme.typography.body_32,
                modifier = Modifier
                    .padding(horizontal = Overscan.horizontalPadding)
                    .padding(top = 4.dp)
            )
        }

        var selectedFilter by remember { mutableStateOf<Pair<String, String>?>(null) }
        val filterRowFocusRequester = remember { FocusRequester() }
        // Disable for now - it brings on a BUNCH of focus issues.
        item.filterAttribute?.let { attribute ->
            FilterSelectorRow(
                attributeValues = attribute.values.map { it.value },
                onValueSelected = { tag -> selectedFilter = tag?.let { attribute.id to it } },
                modifier = Modifier
                    .focusRequester(filterRowFocusRequester)
                    .padding(top = 8.dp)
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
            ItemGrid(filteredItems, modifier = exitFocusModifier)
        } else {
            ItemRow(filteredItems, modifier = exitFocusModifier)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
private fun ItemGrid(items: List<CarouselItem>, modifier: Modifier = Modifier) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .focusRestorer()
            .padding(horizontal = 48.dp)
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
private fun ItemRow(items: List<CarouselItem>, modifier: Modifier = Modifier) {
    // The 'key' function prevents from focusRestorer() from breaking when crashing when
    // filteredItems changes.
    // From what I could tell it's kind of like 'remember' but for Composable.
    key(items) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = modifier.focusRestorer()
        ) {
            spacer(width = 28.dp)
            items(items) { item ->
                CarouselItemCard(
                    carouselItem = item,
                    cardHeight = CARD_HEIGHT,
                )
            }
            spacer(width = 28.dp)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FilterSelectorRow(
    attributeValues: List<String>,
    onValueSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstItemFocusRequester = remember { FocusRequester() }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        modifier = modifier.focusRestorer { firstItemFocusRequester },
    ) {
        spacer(width = 20.dp)
        // TODO: there might not always be an "All" option (e.g. Season 1/2/3 etc)
        item {
            TvButton(
                text = "All",
                onClick = { onValueSelected(null) },
                modifier = Modifier
                    .focusRequester(firstItemFocusRequester)
                    .onFocusChanged {
                        if (it.hasFocus) {
                            onValueSelected(null)
                        }
                    },
            )
        }
        items(attributeValues) { tag ->
            TvButton(
                text = tag,
                onClick = { onValueSelected(tag) },
                modifier = Modifier.onFocusChanged {
                    if (it.hasFocus) {
                        onValueSelected(tag)
                    }
                })
        }
        spacer(width = 20.dp)
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
