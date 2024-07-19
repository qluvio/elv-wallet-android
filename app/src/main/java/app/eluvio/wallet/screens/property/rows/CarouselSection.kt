package app.eluvio.wallet.screens.property.rows

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
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
        // Disable for now - it brings on a BUNCH of focus issues.
//        item.filterAttribute?.let { attribute ->
//            FilterSelectorRow(attribute.tags, onTagSelected = { tag ->
//                selectedFilter = tag?.let { attribute.id to it }
//            })
//        }
        val filteredItems = rememberFilteredItems(item.items, selectedFilter)

        Spacer(modifier = Modifier.height(16.dp))
        // The 'key' function prevents from focusRestorer() from breaking when crashing when
        // filteredItems changes.
        // From what I could tell it's kind of like 'remember' but for Composable.
        key(filteredItems) {
            TvLazyRow(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.focusRestorer() // TODO: figure this shit out
            ) {
                spacer(width = 28.dp)
                items(filteredItems) { item ->
                    CarouselItemCard(
                        carouselItem = item,
                        cardHeight = CARD_HEIGHT,
                    )
                }
                spacer(width = 28.dp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FilterSelectorRow(tags: List<String>, onTagSelected: (String?) -> Unit) {
    TvLazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        modifier = Modifier.focusRestorer(),
    ) {
        spacer(width = 20.dp)
        item {
            TvButton(text = "All", onClick = { onTagSelected(null) })
        }
        items(tags) { tag ->
            TvButton(text = tag, onClick = { onTagSelected(tag) })
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
                        ?.tags?.contains(selectedFilter.second) == true
                }
        }
    }
}
