package com.diskin.alon.sonix.player.infrastructure.model

import android.net.Uri

data class AudioPlayerTrack(val uri: Uri,
                            val isPlaying: Boolean)