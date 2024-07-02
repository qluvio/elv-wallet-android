package app.eluvio.wallet.util.compose

import androidx.compose.ui.Modifier

/**
 * A modifier that will apply the given [block] if the given [condition] is true.
 */
fun Modifier.thenIf(condition: Boolean, block: Modifier.() -> Modifier): Modifier {
    return if (condition) then(Modifier.block()) else this
}
