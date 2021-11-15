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
import java.io.FileOutputStream

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

        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.MediaColumns._ID),
            null,
            null
        )!!

        tracksCount = cursor.count
        cursor.close()

        return tracksCount != 0
    }

    fun copyAudioFilesToDevice(testFilesPaths: List<String>): List<DeviceTrack> {
        val deviceTracks = mutableListOf<DeviceTrack>()
        val context = getApplicationContext<Context>()

        testFilesPaths.forEachIndexed { index, path ->
            if (index > 0) {
                Thread.sleep(1000)
            }

            val trackDeviceFile = File(context.getExternalFilesDir(null)!!.absolutePath,"/${path.split("/").last()}")
            val fis = javaClass.classLoader!!.getResourceAsStream(path)
            val readData = ByteArray(1024 * 500)
            val fos = FileOutputStream(trackDeviceFile)
            var i = fis.read(readData)

            while (i != -1) {
                fos.write(readData, 0, i)
                i = fis.read(readData)
            }

            fos.close()

            val audioCollection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Audio.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                    )
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
            val values = ContentValues(8)
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(trackDeviceFile.absolutePath)
            val trackTitle = path.split("/").last().trimEnd('.','m','p','3')
            val trackDuration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
            val trackAlbum =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "unknown"
            val trackArtist =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "unknown"
            val trackMimeType =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: "unknown"
            retriever.release()

            values.put(MediaStore.Audio.Media.TITLE, trackTitle)
            values.put(MediaStore.Audio.Media.MIME_TYPE, trackMimeType)
            values.put(MediaStore.Audio.Media.DATA, trackDeviceFile.absolutePath)
            values.put(MediaStore.Audio.Media.DURATION,trackDuration)
            values.put(MediaStore.Audio.Media.ALBUM,trackAlbum)
            values.put(MediaStore.Audio.Media.ARTIST,trackArtist)
            values.put(MediaStore.Audio.Media.SIZE, trackDeviceFile.length())
            values.put(MediaStore.Audio.Media.DATE_MODIFIED,System.currentTimeMillis())

            deviceTracks.add(
                DeviceTrack(
                    trackTitle,
                    trackArtist,
                    trackDeviceFile.absolutePath,
                    context.contentResolver.insert(audioCollection,values)!!
                )
            )
        }

        return deviceTracks
    }

    fun deleteFilesFromDevice(filePaths: List<String>) {
        filePaths.forEach { File(it).delete() }
    }

    fun deleteFromMediaStore(uris: List<Uri>) {
        uris.forEach {
            getApplicationContext<Context>()
                .contentResolver.delete(it,null,null)
        }
    }

    /**
     * Clear app preferences from device
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
                           val path: String,
                           val uri: Uri)
}