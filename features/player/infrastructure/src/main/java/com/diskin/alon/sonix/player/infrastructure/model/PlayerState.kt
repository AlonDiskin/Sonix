package com.diskin.alon.sonix.player.infrastructure.model

import android.net.Uri

data class PlayerState(val playbackPosition: Long, val trackIndex: Int, val tracksUri: List<Uri>)