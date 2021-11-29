package com.diskin.alon.sonix.catalog.data

import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.application.util.AppResult
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * [SortingStoreImpl] unit test.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class SortingStoreImplTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var store: SortingStoreImpl

    // Collaborators
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
        ApplicationProvider.getApplicationContext()
    )

    @Before
    fun setUp() {
        store = SortingStoreImpl(sharedPreferences)
    }

    @Test
    fun saveSortingToLocalStorage_WhenSortingSaved() {
        // Given
        val sorting = AudioTracksSorting.ArtistName(true)

        // When
        store.save(sorting).test()
        val observer = store.getLast().test()

        // Then
        observer.assertValue(AppResult.Success(sorting))
    }

    @Test
    fun returnSortingByDateInDescOrderAsDefault_WhenQueriedForLastSorting() {
        // Given

        // When
        val observer = store.getLast().test()

        // Then
        observer.assertValue(AppResult.Success(AudioTracksSorting.DateAdded(false)))
    }
}