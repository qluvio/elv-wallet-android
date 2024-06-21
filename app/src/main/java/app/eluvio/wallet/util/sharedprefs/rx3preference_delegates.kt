package app.eluvio.wallet.util.sharedprefs

import com.frybits.rx.preferences.rx3.Rx3Preference
import kotlin.reflect.KProperty

/**
 * A delegate for a [Rx3Preference] that allows synchronous access to the value.
 * Passing `null` will delete the preference.
 */
operator fun <T : Any> Rx3Preference<T>.setValue(
    thisRef: Any,
    property: KProperty<*>,
    newValue: T?,
) {
    if (newValue == null) {
        delete()
    } else {
        value = newValue
    }
}

/**
 * A delegate for a [Rx3Preference] that allows synchronous access to the value.
 * An unset preference will return `null`.
 */
operator fun <T : Any> Rx3Preference<T>.getValue(
    thisRef: Any,
    property: KProperty<*>
): T? {
    return value.takeIf { isSet }
}
