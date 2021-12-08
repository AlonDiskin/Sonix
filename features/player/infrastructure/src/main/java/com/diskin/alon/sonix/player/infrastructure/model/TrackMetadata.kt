package com.diskin.alon.sonix.player.infrastructure.model

import android.net.Uri

data class TrackMetadata(val name: String,
                         val artist: String,
                         val album: String,
                         val duration: Long,
                         val uri: Uri)