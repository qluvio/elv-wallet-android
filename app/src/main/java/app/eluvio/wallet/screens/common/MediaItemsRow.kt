package app.eluvio.wallet.screens.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import app.eluvio.wallet.data.entities.MediaEntity

@Composable
fun MediaItemsRow(media: List<MediaEntity>) {
    TvLazyRow(
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(media) { media ->
            MediaItemCard(media)
        }
    }
}
