package com.diskin.alon.sonix.catalog.data

import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.sonix.catalog.application.model.ArtistSorting
import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [ArtistsSortingStoreImpl] unit test.
 */
@RunWith(AndroidJUnit4::class)
class ArtistsSortingStoreImplTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var store: ArtistsSortingStoreImpl

    // Collaborators
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
        ApplicationProvider.getApplicationContext()
    )

    @Before
    fun setUp() {
        store = ArtistsSortingStoreImpl(sharedPreferences)
    }

    @Test
    fun saveSortingToLocalStorage_WhenSortingSaved() {
        // Given
        val sorting = ArtistSorting.Date(true)

        // When
        store.save(sorting).test()
        val observer = store.getLast().test()

        // Then
        observer.assertValue(AppResult.Success(sorting))
    }

    @Test
    fun returnSortingByNameInAscendingOrderAsDefault_WhenQueriedForLastSorting() {
        // Given

        // When
        val observer = store.getLast().test()

        // Then
        observer.assertValue(AppResult.Success(ArtistSorting.Name(true)))
    }
}