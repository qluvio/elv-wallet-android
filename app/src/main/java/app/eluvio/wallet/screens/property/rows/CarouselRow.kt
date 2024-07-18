package app.eluvio.wallet.screens.property.rows

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.screens.common.Overscan
import app.eluvio.wallet.screens.common.TvButton
import app.eluvio.wallet.screens.common.spacer
import app.eluvio.wallet.screens.property.CarouselItemCard
import app.eluvio.wallet.screens.property.DynamicPageLayoutState
import app.eluvio.wallet.theme.body_32
import app.eluvio.wallet.theme.carousel_36
import app.eluvio.wallet.util.compose.focusTrap

private val CARD_HEIGHT = 170.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CarouselRow(item: DynamicPageLayoutState.Row.Carousel) {
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
        Spacer(modifier = Modifier.height(16.dp))

        TvLazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.focusRestorer()
        ) {
            spacer(width = 28.dp)
            itemsIndexed(item.items/*.take(3)*/) { index, item ->
                CarouselItemCard(
                    carouselItem = item,
                    cardHeight = CARD_HEIGHT,
                )
            }
            spacer(width = 28.dp)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
