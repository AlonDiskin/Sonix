package com.diskin.alon.sonix.catalog.application.model

data class AudioTrackDetailDto(val name: String,
                               val artist: String,
                               val album: String,
                               val path: String,
                               val size: Long,
                               val duration: Long,
                               val format: String)