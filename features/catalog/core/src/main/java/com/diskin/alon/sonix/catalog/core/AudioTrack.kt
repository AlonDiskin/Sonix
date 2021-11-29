package com.diskin.alon.sonix.catalog.core

data class AudioTrack(val id: Int,
                      val path: String,
                      val name: String,
                      val artist: String,
                      val album: String,
                      val size: Long,
                      val duration: Long,
                      val format: String) {

    init {
        require(path.isNotEmpty())
        require(name.isNotEmpty())
        require(artist.isNotEmpty())
        require(album.isNotEmpty())
        require(size > 0L)
        require(duration > 0L)
        require(format.isNotEmpty())
    }
}