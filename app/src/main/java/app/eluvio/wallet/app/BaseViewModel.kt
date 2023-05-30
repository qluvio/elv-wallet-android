package app.eluvio.wallet.app

import androidx.lifecycle.ViewModel
import app.eluvio.wallet.navigation.Screen
import app.eluvio.wallet.util.asSharedState
import app.eluvio.wallet.util.logging.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject

abstract class BaseViewModel<State : Any>(initialState: State) : ViewModel() {
    private val _navigationEvents = PublishSubject.create<Screen>()
    val navigationEvents: Observable<Screen> =
        _navigationEvents.observeOn(AndroidSchedulers.mainThread())

    private val _state = BehaviorSubject.createDefault(initialState)
    val state = _state
        .doOnSubscribe {
            Log.d("${this.javaClass.simpleName} has started streaming state")
            disposables = CompositeDisposable()
            onStart()
        }
        .doFinally {
            // this is actually a bug, since it'll stop all operations during config changes.
            // not a problem right now, but we need to find a good way to tell config changes apart from putting the app in bg. Maybe a timeout?
            Log.d("${this.javaClass.simpleName} has no more subscribers, disposing all disposables")
            disposables.dispose()
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


    // TODO: is doOnSubscribe really the right starting point?
    protected open fun onStart() {

    }

    protected fun navigateTo(screen: Screen) {
        _navigationEvents.onNext(screen)
    }

    protected fun updateState(mapper: State.() -> State) {
        singleThreadScheduler.scheduleDirect {
            _state.value?.mapper()?.let { _state.onNext(it) }
        }
    }
}
