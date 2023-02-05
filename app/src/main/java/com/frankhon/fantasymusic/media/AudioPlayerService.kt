package com.frankhon.fantasymusic.media

import android.app.Service
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_HEADSET_PLUG
import android.content.IntentFilter
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.session.MediaButtonReceiver
import com.frankhon.fantasymusic.IMusicPlayer
import com.frankhon.fantasymusic.data.settings.KEY_NOTIFICATION_STYLE
import com.frankhon.fantasymusic.data.settings.read
import com.frankhon.fantasymusic.media.AudioPlayer.pause
import com.frankhon.fantasymusic.media.AudioPlayer.release
import com.frankhon.fantasymusic.media.AudioPlayer.setPlaylist
import com.frankhon.fantasymusic.media.AudioPlayer.stop
import com.frankhon.fantasymusic.media.notification.MediaButtonCallback
import com.frankhon.fantasymusic.media.notification.sendMediaNotification
import com.frankhon.fantasymusic.utils.ACTION_SCHEDULE_PAUSE_MUSIC
import com.frankhon.fantasymusic.utils.ACTION_STOP
import com.frankhon.fantasymusic.utils.formatTime
import com.frankhon.fantasymusic.vo.CurrentPlayerInfo
import com.frankhon.fantasymusic.vo.SimpleSong
import com.hon.mylogger.MyLogger

/**
 * Created by Frank Hon on 2020/11/1 7:52 PM.
 * E-mail: frank_hon@foxmail.com
 */
class AudioPlayerService : Service() {

    private val musicPlayer = ServiceStub()

    //用来控制通知栏点击事件
    private val mediaSessionCompat by lazy {
        MediaSessionCompat(this, "AudioPlayerService")
            .apply {
                setCallback(MediaButtonCallback())
            }
    }

    // 定时播放完成的回调
    private val pauseMusicReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                MyLogger.d("pauseMusicReceiver onReceive: ${formatTime(System.currentTimeMillis())}")
                pause()
            }
        }
    }

    // 有线耳机插入和拔出的回调
    private val headsetPlugReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                val state = intent?.getIntExtra("state", -1) ?: -1
                MyLogger.d("headsetPlugReceiver onReceive: $state")
                if (state == 0) {
                    pause()
                }
            }
        }
    }

    // 蓝牙耳机连接和断开的回调
    private val bluetoothHeadsetReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                val state = intent?.getIntExtra(
                    BluetoothProfile.EXTRA_STATE,
                    BluetoothProfile.STATE_DISCONNECTED
                ) ?: BluetoothProfile.STATE_DISCONNECTED
                MyLogger.d("bluetoothHeadsetPlugReceiver onReceive: $state")
                if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    pause()
                }
            }
        }
    }

    override fun onCreate() {
        MyLogger.d("onCreate")
        super.onCreate()
        registerReceiver(pauseMusicReceiver, IntentFilter(ACTION_SCHEDULE_PAUSE_MUSIC))
        registerReceiver(headsetPlugReceiver, IntentFilter(ACTION_HEADSET_PLUG))
        registerReceiver(
            bluetoothHeadsetReceiver,
            IntentFilter().apply {
                addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
            }
        )
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSessionCompat, intent)
        val action = intent.action
        MyLogger.d("onStartCommand: $action")
        if (action == ACTION_STOP) {
            stopMusic()
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
        unregisterReceiver(headsetPlugReceiver)
        unregisterReceiver(bluetoothHeadsetReceiver)
        release()
    }

    private fun stopMusic() {
        stopForeground(false)
        stop()
        release()
    }

    private fun sendMediaNotification() {
        read {
            sendMediaNotification(
                it[KEY_NOTIFICATION_STYLE] ?: true,
                this@AudioPlayerService,
                AudioPlayer.getCurrentPlayerInfo(),
            )
        }
    }

    private inner class ServiceStub : IMusicPlayer.Stub() {
        override fun play(song: SimpleSong) {
            AudioPlayer.play(song)
            sendMediaNotification()
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
            val isPlaylistEmpty = AudioPlayer.removeSongFromPlayList(index)
            if (isPlaylistEmpty) {
                stopMusic()
            }
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