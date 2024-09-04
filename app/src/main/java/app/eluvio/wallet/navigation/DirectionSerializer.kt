package app.eluvio.wallet.navigation

import com.ramcosta.composedestinations.navargs.DestinationsNavTypeSerializer
import com.ramcosta.composedestinations.navargs.NavTypeSerializer
import com.ramcosta.composedestinations.spec.Direction

/**
 * Allows to pass a [Direction] as a navigation argument.
 */
@NavTypeSerializer
class DirectionSerializer : DestinationsNavTypeSerializer<Direction> {
    override fun fromRouteString(routeStr: String): Direction {
        return Direction(routeStr)
    }

    override fun toRouteString(value: Direction): String {
        return value.route
    }
}
