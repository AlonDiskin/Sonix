package com.diskin.alon.sonix.catalog.application

import com.diskin.alon.sonix.catalog.application.interfaces.PlaylistSender
import com.diskin.alon.sonix.catalog.application.model.PlayTracksRequest
import com.diskin.alon.sonix.catalog.application.usecase.PlayTracksUseCase
import com.diskin.alon.sonix.common.application.AppResult
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class PlayTracksUseCaseTest {

    // Test subject
    private lateinit var useCase: PlayTracksUseCase

    // Collaborators
    private val playlistSender: PlaylistSender = mockk()

    @Before
    fun setUp() {
        useCase = PlayTracksUseCase(playlistSender)
    }

    @Test
    fun sendPlayListToPlayer_WhenExecuted() {
        // Given
        val request = PlayTracksRequest(1, mockk())
        var result = mockk<Single<AppResult<Unit>>>()

        every { playlistSender.send(request.startIndex,request.ids) } returns result

        // When
        val useCaseResult = useCase.execute(request)

        // Then
        verify { playlistSender.send(request.startIndex,request.ids) }
        assertThat(useCaseResult).isEqualTo(result)
    }
}