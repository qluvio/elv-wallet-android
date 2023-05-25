package app.eluvio.wallet.app

import androidx.lifecycle.ViewModel
import app.eluvio.wallet.navigation.Screen
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

abstract class BaseViewModel<State : Any> : ViewModel() {
    private val _navigationEvents = PublishSubject.create<Screen>()
    protected abstract val state: Observable<State>
    fun observeState(): Observable<State> = state
    fun observeNavigationEvents(): Observable<Screen> =
        _navigationEvents.observeOn(AndroidSchedulers.mainThread())
    protected fun navigateTo(screen: Screen) {
        _navigationEvents.onNext(screen)
    }
}
