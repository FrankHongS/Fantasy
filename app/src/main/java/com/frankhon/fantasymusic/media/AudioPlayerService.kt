package com.frankhon.fantasymusic.media

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.frankhon.fantasymusic.IMusicPlayer
import com.frankhon.fantasymusic.media.AudioPlayer.next
import com.frankhon.fantasymusic.media.AudioPlayer.pause
import com.frankhon.fantasymusic.media.AudioPlayer.previous
import com.frankhon.fantasymusic.media.AudioPlayer.release
import com.frankhon.fantasymusic.media.AudioPlayer.resume
import com.frankhon.fantasymusic.media.AudioPlayer.setPlaylist
import com.frankhon.fantasymusic.utils.*
import com.frankhon.fantasymusic.vo.CurrentPlayerInfo
import com.frankhon.fantasymusic.vo.SimpleSong
import com.hon.mylogger.MyLogger
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Frank Hon on 2020/11/1 7:52 PM.
 * E-mail: frank_hon@foxmail.com
 */
class AudioPlayerService : Service() {

    private val musicPlayer = ServiceStub()
    private val mainScope by lazy { MainScope() }

    private val pauseMusicReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                MyLogger.d("schedulePause receiver: ${formatTime(System.currentTimeMillis())}")
                pause()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(pauseMusicReceiver, IntentFilter(ACTION_SCHEDULE_PAUSE_MUSIC))
        MyLogger.d("onCreate")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action
        MyLogger.d("onStartCommand: $action")
        if (action != null) {
            when (action) {
                ACTION_PREVIOUS -> previous()
                ACTION_NEXT -> next()
                ACTION_RESUME -> resume()
                ACTION_PAUSE -> pause()
                ACTION_STOP -> stopMusic()
                else -> {}
            }
        }
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder {
        MyLogger.d("onBind")
        return musicPlayer
    }

    override fun onUnbind(intent: Intent): Boolean {
        MyLogger.d("onUnbind")
        // AudioPlayer.release();
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        MyLogger.d("onDestroy")
        super.onDestroy()
        unregisterReceiver(pauseMusicReceiver)
    }

    private fun stopMusic() {
        stopForeground(false)
        releaseMediaSession()
        release()
        mainScope.cancel()
    }

    private fun sendMediaNotification() {
        mainScope.launch {
            readDataStore().collect {
                val style = it[KEY_NOTIFICATION_STYLE]
                sendMediaNotification(
                    style == 0,
                    this@AudioPlayerService,
                    AudioPlayer.getCurrentPlayerInfo(),
                )
            }
        }
    }

    private inner class ServiceStub : IMusicPlayer.Stub() {
        override fun play(song: SimpleSong) {
            AudioPlayer.play(song)
        }

        override fun playAndAddIntoPlaylist(song: SimpleSong) {
            AudioPlayer.playAndAddIntoPlaylist(song)
            sendMediaNotification()
        }

        override fun addIntoPlaylist(song: SimpleSong) {
            val isPlaying = AudioPlayer.addIntoPlaylist(song)
            if (isPlaying) {
                sendMediaNotification()
            }
        }

        override fun removeSongFromPlayList(index: Int) {
            AudioPlayer.removeSongFromPlayList(index)
        }

        override fun setPlayList(playList: List<SimpleSong>, index: Int) {
            setPlaylist(playList, index)
            sendMediaNotification()
        }

        override fun pause() {
            AudioPlayer.pause()
        }

        override fun resume() {
            AudioPlayer.resume()
        }

        override fun next() {
            AudioPlayer.next()
        }

        override fun previous() {
            AudioPlayer.previous()
        }

        override fun seekTo(msec: Int) {
            AudioPlayer.seekTo(msec)
        }

        override fun setPlayMode(playMode: String) {
            AudioPlayer.setPlayMode(playMode)
        }

        override fun getCurrentPlayerInfo(): CurrentPlayerInfo {
            return AudioPlayer.getCurrentPlayerInfo()
        }
    }
}