package app.eluvio.wallet.screens.property.upcoming

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.app.BaseViewModel
import app.eluvio.wallet.data.stores.ContentStore
import app.eluvio.wallet.data.stores.MediaPropertyStore
import app.eluvio.wallet.di.ApiProvider
import app.eluvio.wallet.util.realm.millis
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class UpcomingVideoViewModel @Inject constructor(
    private val contentStore: ContentStore,
    private val propertyStore: MediaPropertyStore,
    private val apiProvider: ApiProvider,
    private val navArgs: UpcomingVideoNavArgs,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<UpcomingVideoViewModel.State>(
    State(mediaItemId = navArgs.mediaItemId, propertyId = navArgs.propertyId),
    savedStateHandle
) {
    data class State(
        val imagesBaseUrl: String? = null,
        private val backgroundImagePath: String? = null,
        val mediaItemId: String = "",
        val propertyId: String = "",
        val title: String = "",
        val icons: List<String> = emptyList(),
        val headers: List<String> = emptyList(),
        val startTimeMillis: Long? = null,
    ) {
        val backgroundImageUrl: String?
            get() = "${imagesBaseUrl}$backgroundImagePath"
                .takeIf { imagesBaseUrl != null && backgroundImagePath != null }
    }

    override fun onResume() {
        super.onResume()

        apiProvider.getFabricEndpoint()
            .subscribeBy { updateState { copy(imagesBaseUrl = it) } }
            .addTo(disposables)

        propertyStore.observeMediaProperty(navArgs.propertyId)
            .subscribeBy {
                updateState { copy(backgroundImagePath = it.mainPage?.backgroundImagePath) }
            }
            .addTo(disposables)

        contentStore.observeMediaItem(navArgs.mediaItemId)
            .subscribeBy(
                onNext = { mediaItem ->
                    updateState {
                        copy(
                            title = mediaItem.name,
                            icons = mediaItem.liveVideoInfo?.icons?.toList().orEmpty(),
                            headers = mediaItem.liveVideoInfo?.headers?.toList().orEmpty(),
                            startTimeMillis = mediaItem.liveVideoInfo?.startTime?.millis
                        )
                    }
                },
                onError = {}
            )
            .addTo(disposables)
    }
}

/**
 * Returns a user-friendly countdown string, and the amount of reaming seconds until the event starts.
 * If the event has already started, the remaining time will be 0.
 * If the event has no start time, this will return null.
 */
val UpcomingVideoViewModel.State.remainingTimeToStart: Pair<String, Long>?
    get() {
        startTimeMillis ?: return null
        val remainingTime = (startTimeMillis - System.currentTimeMillis()).milliseconds
        val remainingSeconds = remainingTime.inWholeSeconds
        if (remainingSeconds <= 0) {
            // Short circuit a time that has already started
            return "0 Seconds" to 0L
        }

        // Build a pretty string we can show the user.
        val remainingTimeStr = remainingTime.toComponents { days, hours, minutes, seconds, _ ->
            buildString {
                /** Copied and modifier from [Duration.toString] */
                val hasDays = days != 0L
                val hasHours = hours != 0
                val hasMinutes = minutes != 0
                val hasSeconds = seconds != 0
                var components = 0
                if (hasDays) {
                    append(days)
                    if (days == 1L) {
                        append(" Day")
                    } else {
                        append(" Days")
                    }
                    components++
                }
                if (hasHours || (hasDays && (hasMinutes || hasSeconds))) {
                    if (components++ > 0) append(", ")
                    append(hours)
                    if (hours == 1) {
                        append(" Hour")
                    } else {
                        append(" Hours")
                    }
                }
                if (hasMinutes || (hasSeconds && (hasHours || hasDays))) {
                    if (components++ > 0) append(", ")
                    append(minutes)
                    if (minutes == 1) append(" Minute") else
                        append(" Minutes")
                }
                // Always show Seconds
                if (components > 0) append(", ")
                append(seconds)
                if (seconds == 1) {
                    append(" Second")
                } else {
                    append(" Seconds")
                }
            }
        }
        return remainingTimeStr to remainingSeconds
    }
