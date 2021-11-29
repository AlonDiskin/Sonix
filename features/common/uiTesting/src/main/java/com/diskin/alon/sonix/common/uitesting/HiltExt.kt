package com.diskin.alon.sonix.common.uitesting

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider

/**
 * launchFragmentInContainer from the androidx.fragment:fragment-testing library
 * is NOT possible to use right now as it uses a hardcoded Activity under the hood
 * (i.e. [EmptyFragmentActivity]) which is not annotated with @AndroidEntryPoint.
 *
 * As a workaround, use this function that is equivalent. It requires you to add
 * [HiltTestActivity] in the debug folder and include it in the debug AndroidManifest.xml file
 * as can be found in this project.
 */
inline fun <reified T : Fragment> launchFragmentInHiltContainer(
    fragmentArgs: Bundle? = null,
    factory: FragmentFactory? = null,
    @StyleRes themeResId: Int = R.style.Theme_MaterialComponents_DayNight,
    crossinline action: Fragment.() -> Unit = {}
): ActivityScenario<HiltTestActivity> {
    val key = "androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY"
    val startActivityIntent = Intent.makeMainActivity(
        ComponentName(
            ApplicationProvider.getApplicationContext(),
            HiltTestActivity::class.java
        )

    ).putExtra(key, themeResId)

    return ActivityScenario.launch<HiltTestActivity>(startActivityIntent).onActivity { activity ->
        val fragment: Fragment = factory?.instantiate(T::class.java.classLoader!!, T::class.java.name) ?: activity.supportFragmentManager.fragmentFactory.instantiate(
            T::class.java.classLoader!!,
            T::class.java.name
        )
        fragment.arguments = fragmentArgs
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "")
            .commitNow()

        fragment.action()
    }
}

inline fun <reified T : DialogFragment> launchDialogInHiltContainer(
    dialogArgs: Bundle? = null,
    crossinline dialogFactory: () -> (DialogFragment)
): ActivityScenario<HiltTestActivity> {
    return ActivityScenario.launch(HiltTestActivity::class.java).onActivity { activity ->
        val dialog = dialogFactory.invoke()
        val tag = dialog.javaClass.name

        dialog.arguments = dialogArgs
        dialog.show(activity.supportFragmentManager, tag)
    }
}