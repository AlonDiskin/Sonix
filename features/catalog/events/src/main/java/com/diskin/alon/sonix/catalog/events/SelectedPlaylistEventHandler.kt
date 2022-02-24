package com.diskin.alon.sonix.catalog.events

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelectedPlaylistEventHandler @Inject constructor() : SelectedPlayListProvider, SelectedPlaylistPublisher {

    private val eventSubject = PublishSubject.create<SelectedPlaylist>()

    override fun get(): Observable<SelectedPlaylist> {
        return eventSubject
    }

    override fun publish(list: SelectedPlaylist) {
        eventSubject.onNext(list)
    }
}