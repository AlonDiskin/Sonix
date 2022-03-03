package com.diskin.alon.sonix.player.playerfeaturetest.scenario.notification_player

import android.app.ActivityManager
import android.content.ContentResolver
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.database.MatrixCursor
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.diskin.alon.sonix.catalog.events.SelectedPlayListProvider
import com.diskin.alon.sonix.catalog.events.SelectedPlaylist
import com.diskin.alon.sonix.player.infrastructure.AudioPlaybackService
import com.diskin.alon.sonix.player.playerfeaturetest.util.EspressoIdlingResource
import com.diskin.alon.sonix.player.playerfeaturetest.util.WhiteBox
import com.diskin.alon.sonix.player.presentation.PlayerFragment
import com.diskin.alon.sonix.player.presentation.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.reactivex.subjects.BehaviorSubject
import java.io.File

class AppOpenedSteps(
    private val playlistProvider: SelectedPlayListProvider,
    private val contentResolver: ContentResolver
) : GreenCoffeeSteps() {

    private lateinit var service: AudioPlaybackService
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var scenario: FragmentScenario<PlayerFragment>
    private lateinit var exoPlayer: ExoPlayer
    private val playListSubject = BehaviorSubject.create<SelectedPlaylist>()
    private val deviceTracks = mapOf(
        Pair(
            Uri.fromFile(File("//android_asset/audio/track_1.mp3")),
            arrayOf("title_1", "artist_1", "album_1", 12000L)
        ),
        Pair(
            Uri.fromFile(File("//android_asset/audio/track_2.mp3")),
            arrayOf("title_2", "artist_2", "album_2", 12000L)
        ),
        Pair(
            Uri.fromFile(File("//android_asset/audio/track_3.mp3")),
            arrayOf("title_3", "artist_3", "album_3", 12000L)
        )
    )

    init {
        val columns = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )

        every { playlistProvider.get() } returns playListSubject
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        deviceTracks.keys.forEach { uri ->
            every { contentResolver.query(uri,columns,null,null,null)
            } returns createMetadataCursor(deviceTracks[uri])
        }

        clearSharedPrefs()
    }

    @Given("^user is listening to selected playlist$")
    fun user_is_listening_to_selected_playlist() {
        // Launch player fragment
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_Sonix)

        // Get exoPLayer instance from service audio player
        waitForAudioServiceCreation()
        service = AudioPlaybackService.service!!
        exoPlayer = WhiteBox.getInternalState(service.player,"exoPlayer") as ExoPlayer
        exoPlayer.setThrowsWhenUsingWrongThread(false)

        // Add listener to player to monitor idling resource and select track to start playback
        service.player.currentTrack.observeForever {
            if (!EspressoIdlingResource.isIdle()) {
                EspressoIdlingResource.decrement()
            }
        }
        EspressoIdlingResource.increment()
        playListSubject.onNext(SelectedPlaylist(0, deviceTracks.keys.toList()))
        waitForIdlingResource()
    }

    @When("^he exit app$")
    fun he_exit_app() {
        device.pressBack()
        Thread.sleep(2000)
    }

    @And("^click on app player notification$")
    fun click_on_app_player_notification() {
        device.openNotification()
        Thread.sleep(2000)
        device.findObject(
            UiSelector()
            .text("title_1"))
            .click()
        Thread.sleep(2000)
    }

    @Then("^app should be opened$")
    fun app_should_be_opened() {
        val am = context.getSystemService(ACTIVITY_SERVICE)  as ActivityManager
        assertThat(am.appTasks[0].taskInfo.topActivity?.packageName)
            .isEqualTo(service.mediaSession.controller.sessionActivity.creatorPackage)
    }

    private fun createMetadataCursor(values: Array<out Any>?): MatrixCursor {
        val cursor = MatrixCursor(
            arrayOf(
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION
            ),
            1
        )

        cursor.addRow(values)
        return cursor
    }

    private fun clearSharedPrefs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

    private fun waitForIdlingResource() {
        while (!EspressoIdlingResource.isIdle()) { Thread.sleep(500) }
    }

    private fun waitForAudioServiceCreation() {
        scenario.onFragment {
            while (!it.mediaBrowser.isConnected) {
                Thread.sleep(500)
            }
        }
    }
}