package app.eluvio.wallet.app

import android.os.Parcelable
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import app.eluvio.wallet.BuildConfig
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.rx.asSharedState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.realm.kotlin.types.BaseRealmObject
import kotlin.reflect.full.memberProperties

private const val STATE_KEY = "viewmodel_state"

abstract class BaseViewModel<State : Any>(
    initialState: State,
    private val savedStateHandle: SavedStateHandle? = null,
) : ViewModel() {
    // TODO: decouple this from Compose.
    private val _navigationEvents = PublishSubject.create<NavigationEvent>()
    val navigationEvents: Observable<NavigationEvent> =
        _navigationEvents.observeOn(AndroidSchedulers.mainThread())

    private val _events = PublishSubject.create<Events>()
    val events: Observable<Events> =
        _events.observeOn(AndroidSchedulers.mainThread())

    private val _state =
        BehaviorSubject.createDefault(savedStateHandle?.get(STATE_KEY) ?: initialState)
    val state = _state
        .doOnSubscribe {
            Log.d("${this.javaClass.simpleName} has started streaming state")
        }
        .distinctUntilChanged()
        .scan { previousState, newState ->
            val printStateUpdates = BuildConfig.DEBUG
            if (printStateUpdates) {
                diffStates(previousState, newState)
                    ?.let { diff ->
                        // Timber will truncate long enough diffs (>4000 chars)
                        val msg = "Diff:\n${diff}"
                        val className = newState.javaClass.name.substringAfterLast('.')
                        Log.d("Next $className emitted: $msg")
                    }
            }
            newState
        }
        .asSharedState()

    /** Synchronize all state updates to happen on a single thread. */
    private val singleThreadScheduler = Schedulers.single()

    // start with a disposed composite because subscriptions aren't valid until the state is streaming
    protected var disposables = CompositeDisposable().apply { dispose() }
        private set

    private var lastLifecycleEvent: Lifecycle.Event = Lifecycle.Event.ON_PAUSE

    // onResume can be called multiple times from the lifecycleOwner without going through onPause in between.
    // This is a workaround to make sure we don't dispose the disposables when that happens.
    fun onResumeTentative() {
        if (lastLifecycleEvent == Lifecycle.Event.ON_PAUSE) {
            onResume()
        }
        lastLifecycleEvent = Lifecycle.Event.ON_RESUME
    }

    @CallSuper
    open fun onResume() {
        Log.d("${this.javaClass.simpleName} onResume")
        disposables.dispose()
        disposables = CompositeDisposable()
    }

    @CallSuper
    open fun onPause() {
        Log.d("${this.javaClass.simpleName} onPause")
        lastLifecycleEvent = Lifecycle.Event.ON_PAUSE
        // this might be a bug, since it'll stop all operations during config changes(?).
        // not a problem right now, but we need to find a good way to tell config changes apart from putting the app in bg. Maybe a timeout?
        disposables.dispose()
    }

    protected fun navigateTo(event: NavigationEvent) {
        _navigationEvents.onNext(event)
    }

    protected fun fireEvent(event: Events) {
        _events.onNext(event)
    }

    protected fun updateState(mapper: State.() -> State) {
        singleThreadScheduler.scheduleDirect {
            _state.value?.mapper()?.let { newState ->
                _state.onNext(newState)
                if (savedStateHandle != null && newState is Parcelable) {
                    savedStateHandle[STATE_KEY] = newState
                }
            }
        }
    }
}

/**
 * Use reflection to diff all declared members of two objects, recursively, and return a human-readable diff.
 * Returns null on either failure, or no diff.
 */
private fun diffStates(oldState: Any, newState: Any): String? {
    fun Any.properties(): List<Pair<String, Any?>> {
        // Assume order is stable and consistent for a given KClass
        return this::class.memberProperties
            .map { prop ->
                prop.name to prop.getter.call(this)
            }
    }

    fun Any.isDiffable(): Boolean = this::class.isData || this is BaseRealmObject
    try {
        return oldState.properties()
            .zip(newState.properties())
            .mapNotNull { (oldKV, newKV) ->
                val (key, oldValue) = oldKV
                val (_, newValue) = newKV
                if (oldValue != newValue) {
                    if (oldValue != null && newValue != null && oldValue.isDiffable()) {
                        "$key:\n${diffStates(oldValue, newValue)?.prependIndent()}"
                    } else {
                        "$key: $oldValue -> $newValue"
                    }
                } else {
                    // No diff
                    null
                }
            }
            .joinToString("\n")
            .takeIf { it.isNotEmpty() }
            ?.prependIndent()
    } catch (e: Exception) {
        return null
    }
}
