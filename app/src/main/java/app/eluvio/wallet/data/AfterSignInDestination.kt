package app.eluvio.wallet.data

import com.ramcosta.composedestinations.spec.Direction
import java.util.concurrent.atomic.AtomicReference

/**
 * Just a dumb singleton that holds onto state between screens.
 * We can't pass arguments to a NavGraph, so we have to use this.
 */
object AfterSignInDestination {
    var direction = AtomicReference<Direction>()
}
