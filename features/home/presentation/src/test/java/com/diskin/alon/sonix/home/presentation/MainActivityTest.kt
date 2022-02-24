package com.diskin.alon.sonix.home.presentation

import android.os.Looper
import androidx.appcompat.view.menu.ActionMenuItem
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/**
 * [MainActivity] hermetic ui test.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@MediumTest
@Config(sdk = [29],application = HiltTestApplication::class)
class MainActivityTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    // Test subject
    private lateinit var scenario: ActivityScenario<MainActivity>

    // Collaborators
    @BindValue
    @JvmField
    val graphProvider: AppGraphProvider = mockk()

    @Before
    fun setUp() {
        // Stub collaborator
        every { graphProvider.getAppGraph() } returns R.navigation.test_app_graph
        every { graphProvider.getPlayerGraph() } returns R.navigation.test_player_graph

        // Launch activity under test
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun setAppGraph_WhenCreated() {
        // Given

        // Then
        scenario.onActivity {
            val controller = it.findNavController(R.id.nav_host_fragment)
            assertThat(controller.graph.startDestination).isEqualTo(R.id.catalogFragment)
        }
    }

    @Test
    fun setPlayerGraph_WhenCreated() {
        // Given

        // Then
        scenario.onActivity {
            val controller = it.findNavController(R.id.nav_player_container)
            assertThat(controller.graph.startDestination).isEqualTo(R.id.stubPlayerFragment)
        }
    }

    @Test
    fun openSettingsScreen_WhenUserNavigates() {
        // Given

        // When
        scenario.onActivity {
            val addMenuItem = ActionMenuItem(
                it,
                0,
                R.id.action_settings,
                0,
                0,
                null
            )

            it.onOptionsItemSelected(addMenuItem)
        }

        // Then
        scenario.onActivity {
            val controller = it.findNavController(R.id.nav_host_fragment)
            assertThat(controller.currentDestination!!.id).isEqualTo(R.id.settingsFragment)
        }
    }
}