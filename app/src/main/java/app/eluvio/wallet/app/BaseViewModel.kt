package app.eluvio.wallet.app

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.util.asSharedState
import app.eluvio.wallet.util.logging.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject

abstract class BaseViewModel<State : Any>(initialState: State) : ViewModel() {
    // TODO: decouple this from Compose.
    private val _navigationEvents = PublishSubject.create<NavigationEvent>()
    val navigationEvents: Observable<NavigationEvent> =
        _navigationEvents.observeOn(AndroidSchedulers.mainThread())

    private val _events = PublishSubject.create<Events>()
    val events: Observable<Events> =
        _events.observeOn(AndroidSchedulers.mainThread())

    private val _state = BehaviorSubject.createDefault(initialState)
    val state = _state
        .doOnSubscribe {
            Log.d("${this.javaClass.simpleName} has started streaming state")
        }
        .doOnNext {
            Log.d("Next state emitted: $it")
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
            _state.value?.mapper()?.let { _state.onNext(it) }
        }
    }
}
