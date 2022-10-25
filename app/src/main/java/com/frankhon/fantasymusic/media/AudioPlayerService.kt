package com.frankhon.fantasymusic.media

import android.app.Service
import android.content.Intent
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

/**
 * Created by Frank Hon on 2020/11/1 7:52 PM.
 * E-mail: frank_hon@foxmail.com
 */
class AudioPlayerService : Service() {

    private val musicPlayer = ServiceStub()
    //private MediaSessionCompat mediaSessionCompat;

    override fun onCreate() {
        super.onCreate()
        MyLogger.d("onCreate")
        //        mediaSessionCompat = new MediaSessionCompat(this, "MediaService");
//        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
//            @Override
//            public void onStop() {
//                stopForeground(true);
//            }
//        });
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
//        MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
        val action = intent.action
        MyLogger.d("onStartCommand: $action")
        if (action != null) {
            when (action) {
                ACTION_PREVIOUS -> previous()
                ACTION_NEXT -> next()
                ACTION_RESUME -> resume()
                ACTION_PAUSE -> pause()
                ACTION_STOP -> {
                    stopForeground(false)
                    releaseMediaSession()
                    release()
                }
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
    }

    private inner class ServiceStub : IMusicPlayer.Stub() {
        override fun play(song: SimpleSong) {
            AudioPlayer.play(song)
        }

        override fun playAndAddIntoPlaylist(song: SimpleSong) {
            AudioPlayer.playAndAddIntoPlaylist(song)
            sendMediaNotification(this@AudioPlayerService, AudioPlayer.getCurrentPlayerInfo(), true)
        }

        override fun addIntoPlaylist(song: SimpleSong) {
            val isPlaying = AudioPlayer.addIntoPlaylist(song)
            if (isPlaying) {
                sendMediaNotification(
                    this@AudioPlayerService,
                    AudioPlayer.getCurrentPlayerInfo(),
                    true
                )
            }
        }

        override fun removeSongFromPlayList(index: Int) {
            AudioPlayer.removeSongFromPlayList(index)
        }

        override fun setPlayList(playList: List<SimpleSong>, index: Int) {
            setPlaylist(playList, index)
            sendMediaNotification(this@AudioPlayerService, AudioPlayer.getCurrentPlayerInfo(), true)
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