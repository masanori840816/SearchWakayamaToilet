package jp.searchwakayamatoilet

/**
 * Created by masanori on 2016/06/26.
 */
import rx.Observable
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject
import rx.subjects.Subject

class RxBus {
    private val bus = SerializedSubject(PublishSubject.create<Any>())

    fun send(o: Any) {
        bus.onNext(o)
    }

    fun toObserverable(): Observable<Any> {
        return bus
    }

    fun hasObservers(): Boolean {
        return bus.hasObservers()
    }
}