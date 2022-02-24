package com.diskin.alon.sonix.catalog.events

import io.reactivex.Observable

interface SelectedPlayListProvider {

    fun get(): Observable<SelectedPlaylist>
}