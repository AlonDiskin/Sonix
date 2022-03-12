package com.diskin.alon.sonix.user_journey

import android.Manifest
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.diskin.alon.sonix.util.DeviceUtil
import com.mauriciotogneri.greencoffee.GreenCoffeeConfig
import com.mauriciotogneri.greencoffee.GreenCoffeeTest
import com.mauriciotogneri.greencoffee.Scenario
import com.mauriciotogneri.greencoffee.ScenarioConfig
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*

@HiltAndroidTest
@RunWith(Parameterized::class)
@LargeTest
class NotificationPlaylistControlStepsRunner(scenario: ScenarioConfig) : GreenCoffeeTest(scenario)  {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun scenarios(): Iterable<ScenarioConfig> {
            return GreenCoffeeConfig()
                .withFeatureFromAssets("assets/feature/notification_playlist_control.feature")
                .scenarios()
        }
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )!!

    private val testSteps = NotificationPlaylistControlSteps()

    @Test
    fun test() {
        start(testSteps)
    }

    override fun afterScenarioEnds(scenario: Scenario?, locale: Locale?) {
        super.afterScenarioEnds(scenario, locale)
        DeviceUtil.clearSharedPrefs()
        DeviceUtil.deleteFilesFromDevice(
            testSteps.deviceTracks.map { it.path }
        )
        DeviceUtil.deleteFromMediaStore(
            testSteps.deviceTracks.map { it.uri }
        )
    }
}