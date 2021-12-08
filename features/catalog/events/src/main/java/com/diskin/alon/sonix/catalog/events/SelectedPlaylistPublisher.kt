package com.diskin.alon.sonix.catalog.events

interface SelectedPlaylistPublisher {

    fun publish(list: SelectedPlaylist)
}