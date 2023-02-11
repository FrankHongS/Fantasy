package com.frankhon.fantasymusic.application

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.IntentFilter
import com.frankhon.fantasymusic.BuildConfig
import com.frankhon.fantasymusic.data.settings.KEY_REMOTE_URL
import com.frankhon.fantasymusic.data.settings.read
import com.frankhon.fantasymusic.media.notification.createNotificationChannel
import com.frankhon.fantasymusic.receivers.MusicInfoReceiver
import com.frankhon.fantasymusic.receivers.MusicPlayerConfigurationReceiver
import com.frankhon.fantasymusic.receivers.MusicProgressReceiver
import com.frankhon.fantasymusic.utils.BASE_URL
import com.frankhon.fantasymusic.utils.MUSIC_INFO_ACTION
import com.frankhon.fantasymusic.utils.MUSIC_PLAYER_CONFIGURATION_ACTION
import com.frankhon.fantasymusic.utils.MUSIC_PROGRESS_ACTION
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

    private val musicInfoReceiver by lazy { MusicInfoReceiver() }
    private val musicPlayerConfigurationReceiver by lazy { MusicPlayerConfigurationReceiver() }
    private val musicProgressReceiver by lazy { MusicProgressReceiver() }

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
        registerMusicReceivers()
    }

    private fun initConfig() {
        read {
            BASE_URL = it[KEY_REMOTE_URL] ?: BASE_URL
        }
    }

    /**
     * 动态注册监听播放器状态的广播接收器（动态广播比静态广播优先级高，避免出现广播接收不到的情况）
     */
    private fun registerMusicReceivers() {
        com.frankhon.fantasymusic.utils.appContext.run {
            registerReceiver(musicInfoReceiver, IntentFilter(MUSIC_INFO_ACTION))
            registerReceiver(
                musicPlayerConfigurationReceiver, IntentFilter(
                    MUSIC_PLAYER_CONFIGURATION_ACTION
                )
            )
            registerReceiver(musicProgressReceiver, IntentFilter(MUSIC_PROGRESS_ACTION))
        }
    }
}