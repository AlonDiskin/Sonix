package com.diskin.alon.sonix.player.infrastructure

import android.os.Handler
import android.os.Looper
import com.google.android.exoplayer2.ExoPlayer
import java.util.concurrent.atomic.AtomicBoolean

class AudioProgressUpdater(
    private val player: ExoPlayer,
    private val listener: (Int) -> (Unit)
) {

    private inner class PlayerObserver : Runnable {

        private val stop = AtomicBoolean(false)
        private val handler = Handler(Looper.getMainLooper())

        override fun run() {
            while (!stop.get()) {
                handler.post {
                    if (player.isPlaying) {
                        listener.invoke(((player.currentPosition.toDouble() / player.duration.toDouble()) * 100).toInt())
                    }
                }

                Thread.sleep(500)
            }
        }

        fun stop() {
            stop.set(true)
        }
    }

    private val playerObserver = PlayerObserver()

    init {
        Thread(playerObserver).start()
    }

    fun release() {
        playerObserver.stop()
    }
}