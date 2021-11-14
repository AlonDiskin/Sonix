package com.diskin.alon.sonix.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until.hasObject
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat

/**
 * Instrumentation device utilities.
 */
object DeviceUtil {

    /**
     *  Opens device home screen.
     */
    fun openDeviceHome() {
        UiDevice.getInstance(getInstrumentation()).pressHome()
    }

    /**
     * Launches app under test in test device.
     */
    fun launchApp() {
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

    fun getDevice(): UiDevice {
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
}