package com.diskin.alon.sonix.catalog.presentation

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/**
 * [CatalogFragment] hermetic ui test class.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(sdk = [29])
class CatalogFragmentTest {

    // Test subject
    private lateinit var scenario: FragmentScenario<CatalogFragment>

    // Collaborators
    private val navController: TestNavHostController = TestNavHostController(ApplicationProvider.getApplicationContext())

    @Before
    fun setUp() {

        // Launch fragment under test
        scenario = launchFragmentInContainer()

        // Set test nav controller
        scenario.onFragment {
            navController.setGraph(R.navigation.catalog_nav_graph)
            Navigation.setViewNavController(it.requireView(), navController)
        }
    }

    @Test
    fun showAppNameAsLabel_WhenResumed() {
        // Given

        // Then
        val expectedLabel = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.app_name)
        assertThat(navController.currentDestination?.label).isEqualTo(expectedLabel)
    }
}