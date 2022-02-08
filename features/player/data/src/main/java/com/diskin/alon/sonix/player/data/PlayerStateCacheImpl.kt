package com.diskin.alon.sonix.player.data

import android.content.SharedPreferences
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.diskin.alon.sonix.player.infrastructure.interfaces.PlayerStateCache
import com.diskin.alon.sonix.player.infrastructure.model.PlayerState
import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class PlayerStateCacheImpl @Inject constructor(private val sp: SharedPreferences)  : PlayerStateCache {

    companion object {
        @VisibleForTesting
        const val KEY_CACHED_STATE = "cached_state"
    }

    private data class PlayerStateJson(val playbackPosition: Long, val trackIndex: Int, val tracksUri: List<String>)

    private val gson = Gson()

    override fun get(): Single<PlayerState> {
        return Single.create<PlayerState> { emitter ->
            val stateStr = sp.getString(KEY_CACHED_STATE,"") ?: ""

            if (stateStr.isNotEmpty()) {
                val state = gson.fromJson(
                    stateStr,
                    PlayerStateJson::class.java
                )

                emitter.onSuccess(
                    PlayerState(
                        state.playbackPosition,
                        state.trackIndex,
                        state.tracksUri.map { Uri.parse(it) }
                    )
                )
            }
        }
            .subscribeOn(Schedulers.io())
    }

    override fun save(state: PlayerState) {
        sp.edit()
            .putString(
                KEY_CACHED_STATE,
                gson.toJson(
                    PlayerStateJson(
                        state.playbackPosition,
                        state.trackIndex,
                        state.tracksUri.map { it.toString() }
                    )
                )
            )
            .apply()
    }
}