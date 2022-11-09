package com.frankhon.fantasymusic.ui.activities

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.receivers.MusicInfoReceiver
import com.frankhon.fantasymusic.receivers.MusicPlayerConfigurationReceiver
import com.frankhon.fantasymusic.receivers.MusicProgressReceiver
import com.frankhon.fantasymusic.ui.fragments.main.MainFragment
import com.frankhon.fantasymusic.utils.MUSIC_INFO_ACTION
import com.frankhon.fantasymusic.utils.MUSIC_PLAYER_CONFIGURATION_ACTION
import com.frankhon.fantasymusic.utils.MUSIC_PROGRESS_ACTION
import com.hon.mylogger.MyLogger

class MainActivity : AppCompatActivity() {

    private var fragment: MainFragment? = null

    private val musicInfoReceiver by lazy { MusicInfoReceiver() }
    private val musicPlayerConfigurationReceiver by lazy { MusicPlayerConfigurationReceiver() }
    private val musicProgressReceiver by lazy { MusicProgressReceiver() }

    override fun onCreate(savedInstanceState: Bundle?) {
        MyLogger.d("onCreate: ")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, MainFragment())
                .commit()
        }
        registerMusicReceivers()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // 点击下载完成的通知，data中包含的数据为content://com.android.providers.downloads.documents/document/1082
        MyLogger.d("onNewIntent: ${intent?.action}, ${intent?.data}")
    }

    /**
     * 有前台服务时，直接杀掉进程，会执行该回调
     */
    override fun onDestroy() {
        MyLogger.d("onDestroy: ")
        super.onDestroy()
        AudioPlayerManager.release()
        unregisterMusicReceivers()
    }

    /**
     * Android 12 behavior changes:
     * (Lifecycle) Root launcher activities are no longer finished on Back press
     */
    override fun onBackPressed() {
        MyLogger.d("onBackPressed: ")
        fragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? MainFragment
        //返回之前先收起控制栏
        fragment?.takeIf { it.isVisible && (it.closeDrawer() || it.collapsePanel()) }
            ?.let { return }
        super.onBackPressed()
    }

    /**
     * 动态注册监听播放器状态的广播接收器（动态广播比静态广播优先级高，避免出现广播接收不到的情况）
     */
    private fun registerMusicReceivers() {
        registerReceiver(musicInfoReceiver, IntentFilter(MUSIC_INFO_ACTION))
        registerReceiver(
            musicPlayerConfigurationReceiver, IntentFilter(
                MUSIC_PLAYER_CONFIGURATION_ACTION
            )
        )
        registerReceiver(musicProgressReceiver, IntentFilter(MUSIC_PROGRESS_ACTION))
    }

    private fun unregisterMusicReceivers() {
        unregisterReceiver(musicInfoReceiver)
        unregisterReceiver(musicPlayerConfigurationReceiver)
        unregisterReceiver(musicProgressReceiver)
    }
}
