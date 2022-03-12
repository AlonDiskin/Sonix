package com.diskin.alon.sonix.catalog.featuretesting.scenario.track_browser

import android.content.ContentResolver
import android.database.ContentObserver
import android.database.MatrixCursor
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.controller.AudioTracksFragment
import com.diskin.alon.sonix.common.uitesting.HiltTestActivity
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.sonix.common.uitesting.launchFragmentInHiltContainer
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog

class TrackDeletedSteps(
    private val contentResolver: ContentResolver
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val deviceTracks: MutableList<DeviceTrack> = createDeiceTracks().toMutableList()
    private var lastDeletedId = -1

    @Given("^user has public audio tracks on device$")
    fun user_has_public_audio_tracks_on_device() {
        // Create test tracks by stubbing mock content resolver
        val observerSlot = slot<ContentObserver>()

        every { contentResolver.query(any(),any(),any(),any(),any()) } answers {
            val trackColumns = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
            )
            val cursor = MatrixCursor(trackColumns, deviceTracks.size)

            deviceTracks.forEach {
                cursor.addRow(
                    arrayOf(
                        it.id,
                        it.name,
                        it.album,
                        it.artist,
                        it.format,
                        it.size,
                        it.path,
                        it.duration
                    )
                )
            }

            cursor
        }
        every { contentResolver.registerContentObserver(any(),any(),capture(observerSlot)) } returns Unit
        every { contentResolver.unregisterContentObserver(any()) } returns Unit
        every { contentResolver.delete(any(),null,null) } answers {
            // find deleted id from given uri in cursor, remove row and update slot observer
            val uri = args[0] as Uri
            val id = uri.lastPathSegment!!.toInt()
            val deletedTrack = deviceTracks.find { it.id == id }
            val observer = observerSlot.captured
            lastDeletedId = id

            deviceTracks.remove(deletedTrack)
            observer.onChange(true)
            1
        }
    }

    @When("^user open audio browser screen$")
    fun user_open_audio_browser_screen() {
        scenario = launchFragmentInHiltContainer<AudioTracksFragment>()

        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @And("^user select to delete first and last listed tracks$")
    fun user_select_to_delete_first_and_last_listed_tracks() {
        onView(withRecyclerView(R.id.tracks).atPositionOnView(0, R.id.track_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView((withText(R.string.title_action_delete_track)))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        (ShadowAlertDialog.getLatestDialog() as AlertDialog)
            .getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Block the testing thread so that list adapter thread could finish, currently no known sync method
        // other the block with sleep
        Thread.sleep(2000)
    }

    @Then("^app should delete selected tracks from device$")
    fun app_should_delete_selected_tracks_from_device() {
        val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        verify {
            contentResolver.delete(
                Uri.parse(
                    contentUri.toString()
                        .plus("/${lastDeletedId}")
                ),
                null,
                null
            )
        }
    }

    @And("^app should update shown listed tracks$")
    fun app_should_update_shown_listed_tracks() {
        verifyDeviceTracksShow(deviceTracks)
    }
}