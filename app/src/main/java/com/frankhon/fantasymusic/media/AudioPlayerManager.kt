package com.frankhon.fantasymusic.media

import com.frankhon.fantasymusic.IMusicPlayer
import android.content.ServiceConnection
import android.content.ComponentName
import android.content.Context
import android.os.IBinder
import android.content.Intent
import android.os.RemoteException
import com.frankhon.fantasymusic.Fantasy
import com.frankhon.fantasymusic.vo.SimpleSong

/**
 * Created by Frank Hon on 2020/11/1 8:26 PM.
 * E-mail: frank_hon@foxmail.com
 */
object AudioPlayerManager {
    private var musicPlayer: IMusicPlayer? = null
    private var onServiceConnectedListener: (AudioPlayerManager.() -> Unit)? = null
    private var hasBoundService = false
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            musicPlayer = IMusicPlayer.Stub.asInterface(service)
            hasBoundService = true
            onServiceConnectedListener?.invoke(this@AudioPlayerManager)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            hasBoundService = false
        }
    }

    @JvmStatic
    fun connect(listener: (AudioPlayerManager.() -> Unit)? = null) {
        this.onServiceConnectedListener = listener
        if (hasBoundService) {
            onServiceConnectedListener?.invoke(this)
        } else {
            val intent = Intent(Fantasy.getAppContext(), AudioPlayerService::class.java)
            Fantasy.getAppContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    @JvmStatic
    fun clear() {
        Fantasy.getAppContext().unbindService(connection)
    }

    @JvmStatic
    fun setPlayList(playList: List<SimpleSong?>?) {
        setPlayList(playList, 0)
    }

    @JvmStatic
    fun setPlayList(playList: List<SimpleSong?>?, index: Int) {
        try {
            musicPlayer!!.setPlayList(playList, index)
        } catch (e: RemoteException) {
            // do nothing
        }
    }

    @JvmStatic
    fun play(song: SimpleSong?) {
        try {
            musicPlayer!!.play(song)
        } catch (e: RemoteException) {
            // do nothing
        }
    }

    @JvmStatic
    fun pause() {
        try {
            musicPlayer!!.pause()
        } catch (e: RemoteException) {
            // do nothing
        }
    }

    @JvmStatic
    fun resume() {
        try {
            musicPlayer!!.resume()
        } catch (e: RemoteException) {
            // do nothing
        }
    }

    @JvmStatic
    fun next() {
        try {
            musicPlayer!!.next()
        } catch (e: RemoteException) {
            // do nothing
        }
    }

    @JvmStatic
    fun previous() {
        try {
            musicPlayer!!.previous()
        } catch (e: RemoteException) {
            // do nothing
        }
    }

    @JvmStatic
    fun seekTo(msec: Int) {
        try {
            musicPlayer!!.seekTo(msec)
        } catch (e: RemoteException) {
            // do nothing
        }
    }
}