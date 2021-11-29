package com.diskin.alon.sonix.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.test.espresso.IdlingRegistry
import androidx.test.runner.AndroidJUnitRunner
import com.diskin.alon.sonix.R
import com.diskin.alon.sonix.common.uitesting.setFinalStatic
import com.diskin.alon.sonix.home.presentation.MainActivity
import com.squareup.rx2.idler.Rx2Idler
import dagger.hilt.android.testing.HiltTestApplication
import io.reactivex.plugins.RxJavaPlugins

class CustomTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        val app =  super.newApplication(cl, HiltTestApplication::class.java.name, context)

        // Register loading idling resource
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            private lateinit var loadingIdlingResource: LoadingIdlingResource

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if(activity is MainActivity) {
                    loadingIdlingResource = LoadingIdlingResource(activity as FragmentActivity, R.id.progressBar)
                    IdlingRegistry.getInstance().register(loadingIdlingResource)

                }
            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {
                if(activity is MainActivity) {
                    IdlingRegistry.getInstance().unregister(loadingIdlingResource)
                }
            }
        })

        return app
    }

    override fun onStart() {
        super.onStart()

        // Register espresso idling resource
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)

        // Disable data binding Choreographer
        setFinalStatic(ViewDataBinding::class.java.getDeclaredField("USE_CHOREOGRAPHER"),false)

        // Init RxIdler
        RxJavaPlugins.setInitIoSchedulerHandler(Rx2Idler.create("RxJava 2.x IO Scheduler"))
    }
}