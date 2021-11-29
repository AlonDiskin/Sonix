package com.diskin.alon.sonix.catalog.presentation.util

import android.content.res.Resources
import com.diskin.alon.sonix.catalog.application.model.AudioTrackDetailDto
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.model.UiAudioTrackDetail
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.time.DurationFormatUtils
import javax.inject.Inject

class ModelTrackDetailMapper @Inject constructor(
    private val resources: Resources
) {

    fun map(track: AudioTrackDetailDto): UiAudioTrackDetail {
        return UiAudioTrackDetail(
            track.name,
            track.artist,
            track.album,
            track.path,
            FileUtils.byteCountToDisplaySize(track.size),
            DurationFormatUtils.formatDuration(
                track.duration,
                resources.getString(R.string.track_detail_duration_format),
                true
            ),
            track.format
        )
    }
}