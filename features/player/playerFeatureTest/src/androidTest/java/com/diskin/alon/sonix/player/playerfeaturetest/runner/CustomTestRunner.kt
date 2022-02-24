package com.diskin.alon.sonix.player.playerfeaturetest.runner

import android.app.Application
import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.test.runner.AndroidJUnitRunner
import com.diskin.alon.sonix.common.uitesting.setFinalStatic
import com.squareup.rx2.idler.Rx2Idler
import dagger.hilt.android.testing.HiltTestApplication
import io.reactivex.plugins.RxJavaPlugins

class CustomTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }

    override fun onStart() {
        super.onStart()

        // Disable data binding Choreographer
        setFinalStatic(ViewDataBinding::class.java.getDeclaredField("USE_CHOREOGRAPHER"),false)

        // Init RxIdler
        RxJavaPlugins.setInitIoSchedulerHandler(Rx2Idler.create("RxJava 2.x IO Scheduler"))
    }
}