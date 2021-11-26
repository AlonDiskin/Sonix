package com.diskin.alon.sonix.catalog.presentation

import android.Manifest
import android.content.Context
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.sonix.catalog.presentation.controller.CatalogFragment
import com.diskin.alon.sonix.catalog.presentation.controller.CatalogFragmentFactory
import com.diskin.alon.sonix.catalog.presentation.controller.EmptyFragment
import com.google.android.material.tabs.TabLayout
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowAlertDialog

/**
 * [CatalogFragment] unit test class.
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

    // Stub data
    private val testRegistry = object : ActivityResultRegistry() {
        private var _permissionsRequested = false
        val permissionsRequested get() = _permissionsRequested
        var permissionResult = true
        override fun <I : Any?, O : Any?> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?
        ) {
            _permissionsRequested = true
            when(contract) {
                is ActivityResultContracts.RequestMultiplePermissions -> {
                    if ((input as Array<String>)[0] == Manifest.permission.READ_EXTERNAL_STORAGE &&
                        (input as Array<String>)[1] == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                        dispatchResult(
                            requestCode,
                            mapOf(
                                Pair(Manifest.permission.READ_EXTERNAL_STORAGE,permissionResult),
                                Pair(Manifest.permission.WRITE_EXTERNAL_STORAGE,permissionResult)
                            )
                        )
                    }
                }
            }
        }
    }

    @Before
    fun setUp() {
        // Mock pager fragments instantiation
        mockkConstructor(CatalogFragmentFactory::class)
        every { anyConstructed<CatalogFragmentFactory>().create(any()) } returns EmptyFragment()

        // Launch fragment under test
        scenario = launchFragmentInContainer (
            themeResId = R.style.Theme_MaterialComponents_DayNight,
            factory = object : FragmentFactory() {
                override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                    return CatalogFragment(testRegistry)
                }
            }
        )

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

    @Test
    fun askUserForStoragePermission_WhenCreatedWithoutPermissionGranted() {
        // Given

        // Then
        assertThat(testRegistry.permissionsRequested).isTrue()
    }

    @Test
    fun showAppPermissionDialog_WhenUserDenyStoragePermission() {
        // Given
        testRegistry.permissionResult = false

        // When
        scenario.recreate()

        // Then
        val dialog = (ShadowAlertDialog.getLatestDialog() as AlertDialog)
        assertThat(dialog.isShowing).isTrue()
    }

    @Test
    fun showTracksFragmentAndTabFirst_WhenCreated() {
        // Given

        // Then
        verify { anyConstructed<CatalogFragmentFactory>().create(CatalogFragmentFactory.Type.TRACKS) }
        scenario.onFragment {
            val tabs = it.view!!.findViewById<TabLayout>(R.id.tab_layout)

            assertThat(tabs.getTabAt(0)!!.text).isEqualTo(it.getString(R.string.tab_title_tracks))
        }
    }
}