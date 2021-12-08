package com.diskin.alon.sonix.player.featuretesting

import android.content.ContentResolver
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.MediumTest
import com.diskin.alon.sonix.catalog.events.SelectedPlayListProvider
import com.diskin.alon.sonix.player.infrastructure.AudioPlayer
import com.mauriciotogneri.greencoffee.GreenCoffeeConfig
import com.mauriciotogneri.greencoffee.GreenCoffeeTest
import com.mauriciotogneri.greencoffee.ScenarioConfig
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import javax.inject.Inject

@HiltAndroidTest
@RunWith(Parameterized::class)
@MediumTest
class PlaySelectedPlayListStepsRunner(scenario: ScenarioConfig) : GreenCoffeeTest(scenario) {

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun scenarios(): Iterable<ScenarioConfig> {
            return GreenCoffeeConfig()
                .withFeatureFromAssets("assets/feature/play_selected_playlist.feature")
                .withTags("@play-selected")
                .scenarios()
        }
    }

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var playlistProvider: SelectedPlayListProvider

    @Inject
    lateinit var contentResolver: ContentResolver

    @Inject
    lateinit var player: AudioPlayer

    @Test
    fun test() {
        // Inject test dependencies
        hiltRule.inject()

        start(PlaySelectedPlaylistSteps(playlistProvider,contentResolver,player))
    }
}