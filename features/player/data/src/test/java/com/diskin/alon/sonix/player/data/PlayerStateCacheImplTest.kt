package com.diskin.alon.sonix.player.data

import android.net.Uri
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.sonix.player.infrastructure.model.PlayerState
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerStateCacheImplTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var cache: PlayerStateCacheImpl

    // Collaborators
    private val sp = PreferenceManager.getDefaultSharedPreferences(
        ApplicationProvider.getApplicationContext()
    )

    @Before
    fun setUp() {
        // Init subject
        cache = PlayerStateCacheImpl(sp, mockk())
    }

    @Test
    fun saveAndRetrievePlayerState() {
        // Given
        val state = PlayerState(10L,2, listOf(Uri.parse("uri_scheme")))

        // When
        cache.save(state)
        val observer = cache.get().test()

        // Then
        assertThat(sp.contains(PlayerStateCacheImpl.KEY_CACHED_STATE)).isTrue()
        observer.assertValue(state)
    }
}