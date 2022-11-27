package com.frankhon.fantasymusic.application

import android.app.Activity
import android.app.Application
import android.content.IntentFilter
import android.os.Bundle
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.receivers.MusicInfoReceiver
import com.frankhon.fantasymusic.receivers.MusicPlayerConfigurationReceiver
import com.frankhon.fantasymusic.receivers.MusicProgressReceiver
import com.frankhon.fantasymusic.utils.MUSIC_INFO_ACTION
import com.frankhon.fantasymusic.utils.MUSIC_PLAYER_CONFIGURATION_ACTION
import com.frankhon.fantasymusic.utils.MUSIC_PROGRESS_ACTION
import com.frankhon.fantasymusic.utils.appContext
import com.hon.mylogger.MyLogger

/**
 * Created by Frank Hon on 2022/8/30 10:11 上午.
 * E-mail: frank_hon@foxmail.com
 */
class FantasyActivityLifecycleCallback : Application.ActivityLifecycleCallbacks {

    private var activityCount = 0

    private val musicInfoReceiver by lazy { MusicInfoReceiver() }
    private val musicPlayerConfigurationReceiver by lazy { MusicPlayerConfigurationReceiver() }
    private val musicProgressReceiver by lazy { MusicProgressReceiver() }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        MyLogger.d("onActivityCreated: ")
        activityCount++
        if (activityCount == 1) {
            registerMusicReceivers()
        }
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
        activityCount--
        if (activityCount == 0) {
            AudioPlayerManager.release()
            unregisterMusicReceivers()
        }
    }

    /**
     * 动态注册监听播放器状态的广播接收器（动态广播比静态广播优先级高，避免出现广播接收不到的情况）
     */
    private fun registerMusicReceivers() {
        appContext.run {
            registerReceiver(musicInfoReceiver, IntentFilter(MUSIC_INFO_ACTION))
            registerReceiver(
                musicPlayerConfigurationReceiver, IntentFilter(
                    MUSIC_PLAYER_CONFIGURATION_ACTION
                )
            )
            registerReceiver(musicProgressReceiver, IntentFilter(MUSIC_PROGRESS_ACTION))
        }
    }

    private fun unregisterMusicReceivers() {
        appContext.run {
            unregisterReceiver(musicInfoReceiver)
            unregisterReceiver(musicPlayerConfigurationReceiver)
            unregisterReceiver(musicProgressReceiver)
        }
    }
}