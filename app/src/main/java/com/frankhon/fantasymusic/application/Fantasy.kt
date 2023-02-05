package com.frankhon.fantasymusic.application

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.frankhon.fantasymusic.BuildConfig
import com.frankhon.fantasymusic.data.settings.KEY_REMOTE_URL
import com.frankhon.fantasymusic.data.settings.read
import com.frankhon.fantasymusic.media.notification.createNotificationChannel
import com.frankhon.fantasymusic.utils.BASE_URL
import com.hon.mylogger.MyCrashHandler
import com.hon.mylogger.MyLogger

/**
 * Created by Frank_Hon on 1/6/2020.
 * E-mail: v-shhong@microsoft.com
 */
class Fantasy : Application() {

    companion object {
        @JvmStatic
        @SuppressLint("StaticFieldLeak")
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        registerActivityLifecycleCallbacks(FantasyActivityLifecycleCallback())
        createNotificationChannel()
        MyLogger.setLoggable(BuildConfig.DEBUG)
        //将日志记录到本地
        MyLogger.initLogFilePath(filesDir.path)
        //记录崩溃日志
        MyCrashHandler.init(BuildConfig.VERSION_NAME)

        initConfig()
    }

    private fun initConfig() {
        read {
            BASE_URL = it[KEY_REMOTE_URL] ?: BASE_URL
        }
    }
}