package com.diskin.alon.sonix.util

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until.hasObject
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import java.io.File

/**
 * Instrumentation device utilities.
 */
object DeviceUtil {

    /**
     *  Opens device home screen.
     */
    private fun openDeviceHome() {
        UiDevice.getInstance(getInstrumentation()).pressHome()
    }

    /**
     * Launches app under test in test device.
     */
    private fun launchApp() {
        val timeout = 5000L
        val launcherPackage =
            getLaunchPackageName()
        assertThat(launcherPackage, notNullValue())
        UiDevice.getInstance(getInstrumentation())
            .wait(hasObject(By.pkg(launcherPackage).depth(0)), timeout)

        // Launch the blueprint app
        val context = getApplicationContext<Context>()
        val appPackage = context.packageName
        val intent = context.packageManager
            .getLaunchIntentForPackage(appPackage)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)    // Clear out any previous instances
        context.startActivity(intent)

        // Wait for the app to appear
        UiDevice.getInstance(getInstrumentation())
            .wait(hasObject(By.pkg(appPackage).depth(0)), timeout)
    }

    fun rotateDeviceToLandsacpe() {
        UiDevice.getInstance(getInstrumentation())
            .setOrientationLeft()
    }

    fun rotateDeviceToPortrait() {
        UiDevice.getInstance(getInstrumentation())
            .setOrientationNatural()
    }

    fun rotateDeviceLand() {
        UiDevice.getInstance(getInstrumentation()).setOrientationLeft()
    }

    fun rotateDevicePort() {
        UiDevice.getInstance(getInstrumentation()).setOrientationNatural()
    }

    fun pressBack() {
        UiDevice.getInstance(getInstrumentation()).pressBack()
    }

    private fun getDevice(): UiDevice {
        return UiDevice.getInstance(getInstrumentation())
    }

    fun grantStorageAccessPermission() {
        getInstrumentation().uiAutomation.grantRuntimePermission(
            getInstrumentation().targetContext.packageName,
            Manifest.permission.READ_EXTERNAL_STORAGE)

        getInstrumentation().uiAutomation.grantRuntimePermission(
            getInstrumentation().targetContext.packageName,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun getLaunchPackageName(): String {
        // Create launcher Intent
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)

        // Use PackageManager to get the launcher package name
        val pm = getInstrumentation().context.packageManager
        val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

        return resolveInfo?.activityInfo?.packageName ?: ""
    }

    fun launchAppFromHome() {
        openDeviceHome()
        launchApp()
    }

    fun disconnectNetwork() {
        UiDevice.getInstance(getInstrumentation())
            .executeShellCommand("svc wifi disable")
        UiDevice.getInstance(getInstrumentation())
            .executeShellCommand("svc data disable")
    }

    fun connectNetwork() {
        UiDevice.getInstance(getInstrumentation())
            .executeShellCommand("svc wifi enable")
        UiDevice.getInstance(getInstrumentation())
            .executeShellCommand("svc data enable")
    }

    fun approveLocationDialogIfExist() {
        val dialogShowing: Boolean = getDevice().wait(
            hasObject(By.textContains("location service")),
            5000L
        )

        if (dialogShowing) {
            getDevice().findObject(UiSelector().text("OK")).click()
        }
    }

    fun checkIfDeviceHasPublicAudioTracks(): Boolean {
        val context = getApplicationContext<Context>()
        val contentResolver = context.contentResolver!!
        val tracksCount: Int
        val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val cursor = contentResolver.query(
            contentUri,
            arrayOf(MediaStore.MediaColumns._ID),
            null,
            null
        )!!

        tracksCount = cursor.count
        cursor.close()

        return tracksCount != 0
    }

    fun copyAudioFilesToDevice(testFilesPaths: List<String>): List<DeviceTrack> {
        val context = getApplicationContext<Context>()
        val deviceTracks = mutableListOf<DeviceTrack>()
        val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        testFilesPaths.forEach { path ->
            Thread.sleep(1000)

            val values = ContentValues(3)

            values.put(MediaStore.Audio.Media.DISPLAY_NAME, path.split("/").last().trimEnd('.','m','p','3'))
            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg") // audio/mpeg
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, "Music")

            val resolver = context.contentResolver
            val uri = resolver.insert(contentUri, values)

            resolver.openOutputStream(uri!!).use { fos ->
                val fis = javaClass.classLoader!!.getResourceAsStream(path)
                val readData = ByteArray(1024 * 500)
                var i = fis.read(readData)

                while (i != -1) {
                    fos!!.write(readData, 0, i)
                    i = fis.read(readData)
                }

                fos!!.close()
            }

            val trackFileName = path.split("/").last().trimEnd('.','m','p','3')
            val trackPath = "/storage/emulated/0/Music/".plus(trackFileName).plus(".mp3")
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(trackPath)
            val trackTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)!!
            val trackDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
            val trackAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)!!
            val trackArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)!!
            val trackMimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)!!

            deviceTracks.add(
                DeviceTrack(
                    trackTitle,
                    trackArtist,
                    trackAlbum,
                    trackMimeType,
                    trackPath,
                    trackDuration,
                    uri
                )
            )

            retriever.release()
        }

        return deviceTracks
    }

    fun deleteFilesFromDevice(filePaths: List<String>) {
        filePaths.forEach { File(it).delete() }
    }

    /**
     * Delete media items from device media store.
     */
    fun deleteFromMediaStore(uris: List<Uri>) {
        uris.forEach {
            getApplicationContext<Context>()
                .contentResolver.delete(it,null,null)
        }
    }

    /**
     * Clear app preferences from device.
     */
    fun clearSharedPrefs() {
        val context = getApplicationContext<Context>()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.clear()
        editor.commit()
    }

    data class DeviceTrack(val title:String,
                           val artist: String,
                           val album: String,
                           val format: String,
                           val path: String,
                           val duration: Long,
                           val uri: Uri)
}