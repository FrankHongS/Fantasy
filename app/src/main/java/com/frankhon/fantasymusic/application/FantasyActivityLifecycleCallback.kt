package com.frankhon.fantasymusic.application

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.hon.mylogger.MyLogger

/**
 * Created by Frank Hon on 2022/8/30 10:11 上午.
 * E-mail: frank_hon@foxmail.com
 */
class FantasyActivityLifecycleCallback : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        MyLogger.d("onActivityCreated: ")

    }

    override fun onActivityStarted(activity: Activity) {
        MyLogger.d("onActivityStarted: ")
    }

    override fun onActivityResumed(activity: Activity) {
        MyLogger.d("onActivityResumed: ")
    }

    override fun onActivityPaused(activity: Activity) {
        MyLogger.d("onActivityPaused: ")
    }

    override fun onActivityStopped(activity: Activity) {
        MyLogger.d("onActivityStopped: ")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        MyLogger.d("onActivitySaveInstanceState: ")
    }

    override fun onActivityDestroyed(activity: Activity) {
        MyLogger.d("onActivityDestroyed: ")
    }
}