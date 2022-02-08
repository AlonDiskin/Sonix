package com.diskin.alon.sonix.player.infrastructure.interfaces

import com.diskin.alon.sonix.player.infrastructure.model.PlayerState
import io.reactivex.Single

interface PlayerStateCache {

    fun get(): Single<PlayerState>

    fun save(state: PlayerState)
}