package com.diskin.alon.sonix.catalog.presentation

import com.diskin.alon.sonix.catalog.application.model.AudioTrackDto
import com.diskin.alon.sonix.catalog.presentation.model.UiAudioTrack
import com.diskin.alon.sonix.catalog.presentation.util.ModelTracksMapper
import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [ModelTracksMapper] integration test class.
 */
@RunWith(JUnitParamsRunner::class)
class ModelTracksMapperTest {

    // Test subject
    private lateinit var mapper: ModelTracksMapper

    @Before
    fun setUp() {
        mapper = ModelTracksMapper()
    }

    @Test
    @Parameters(method = "tracksParams")
    fun mapModelTracks(modelTracks: List<AudioTrackDto>,uiTracks: List<UiAudioTrack>) {
        // Given

        // When
        val actual = mapper.map(modelTracks)

        // Then
        assertThat(actual).isEqualTo(uiTracks)
    }

    private fun tracksParams() = arrayOf(
        arrayOf(
            listOf(
                AudioTrackDto(1,"name_1","artist_1"),
                AudioTrackDto(2,"name_2","artist_2"),
                AudioTrackDto(3,"name_3","artist_3")
            ),
            listOf(
                UiAudioTrack(1,"name_1","artist_1"),
                UiAudioTrack(2,"name_2","artist_2"),
                UiAudioTrack(3,"name_3","artist_3")
            )
        )
    )
}