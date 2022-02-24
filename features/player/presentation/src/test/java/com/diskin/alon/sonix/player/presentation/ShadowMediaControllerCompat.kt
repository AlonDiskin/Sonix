package com.diskin.alon.sonix.player.presentation

import android.content.Context
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import org.robolectric.annotation.Implements

@Implements(MediaControllerCompat::class)
class ShadowMediaControllerCompat {

    fun __constructor__(context: Context, token: MediaSessionCompat.Token) {}
}