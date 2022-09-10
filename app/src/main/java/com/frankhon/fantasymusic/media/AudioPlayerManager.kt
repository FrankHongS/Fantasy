package com.frankhon.fantasymusic.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import com.frankhon.fantasymusic.IMusicPlayer
import com.frankhon.fantasymusic.application.Fantasy
import com.frankhon.fantasymusic.media.observer.AudioLifecycleObserver
import com.frankhon.fantasymusic.media.observer.AudioProgressObserver
import com.frankhon.fantasymusic.vo.SimpleSong
import com.hon.mylogger.MyLogger

/**
 * Created by Frank Hon on 2020/11/1 8:26 PM.
 * E-mail: frank_hon@foxmail.com
 */
object AudioPlayerManager {
    private var musicPlayer: IMusicPlayer? = null
    private var onServiceConnectedListener: ((AudioPlayerManager) -> Unit)? = null
    private var hasBoundService = false
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            musicPlayer = IMusicPlayer.Stub.asInterface(service)
            invokeOnServiceConnected()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            hasBoundService = false
        }
    }

    private val lifecycleObservers = mutableListOf<AudioLifecycleObserver>()
    private val progressObservers = mutableListOf<AudioProgressObserver>()

    /**
     * @param listener 绑定服务之后的操作，不要在该监听器中做播放操作，因为此时不一定完成服务的绑定
     */
    @JvmStatic
    fun connect(listener: ((AudioPlayerManager) -> Unit)? = null) {
        if (hasBoundService) {
            listener?.invoke(this)
        } else {
            this.onServiceConnectedListener = listener
            hasBoundService = true
            val intent = Intent(Fantasy.getAppContext(), AudioPlayerService::class.java)
            Fantasy.getAppContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
            Fantasy.getAppContext().startService(intent)
        }
    }

    private fun invokeOnServiceConnected() {
        onServiceConnectedListener?.invoke(this)
        // avoid memory leak from anonymous inner class
        onServiceConnectedListener = null
    }

    @JvmStatic
    fun registerLifecycleObserver(observer: AudioLifecycleObserver) {
        if (!lifecycleObservers.contains(observer)) {
            lifecycleObservers.add(observer)
            observer.onPlayerConnected(getCurrentPlayerInfo())
        }
    }

    @JvmStatic
    fun unregisterLifecycleObserver(observer: AudioLifecycleObserver) {
        lifecycleObservers.remove(observer)
    }

    @JvmStatic
    fun registerProgressObserver(observer: AudioProgressObserver) {
        if (!progressObservers.contains(observer)) {
            progressObservers.add(observer)
        }
    }

    @JvmStatic
    fun unregisterProgressObserver(observer: AudioProgressObserver) {
        progressObservers.remove(observer)
    }

    @JvmStatic
    fun release() {
        Fantasy.getAppContext().unbindService(connection)
        hasBoundService = false
        lifecycleObservers.clear()
        progressObservers.clear()
    }

    @JvmStatic
    fun setPlayList(playList: List<SimpleSong>) {
        setPlayList(playList, 0)
    }

    @JvmStatic
    fun setPlayList(playList: List<SimpleSong>, index: Int) {
        musicPlayer?.setPlayList(playList, index)
    }

    @JvmStatic
    fun play(song: SimpleSong?) {
        musicPlayer?.play(song)
    }

    @JvmStatic
    fun pause() {
        musicPlayer?.pause()
    }

    @JvmStatic
    fun resume() {
        musicPlayer?.resume()
    }

    @JvmStatic
    fun next() {
        musicPlayer?.next()
    }

    @JvmStatic
    fun previous() {
        musicPlayer?.previous()
    }

    @JvmStatic
    fun seekTo(msec: Int) {
        musicPlayer?.seekTo(msec)
    }

    @JvmStatic
    fun getCurrentPlayerInfo() = musicPlayer?.currentPlayerInfo

    fun publishPlayerState(song: SimpleSong?, state: PlayerState, errorMsg: String) {
        notifyLifecycleObserver(song, state, errorMsg)
    }

    fun publishPlayerProgress(curPosition: Long, duration: Long) {
        notifyProgressObserver(curPosition, duration)
    }

    private fun notifyLifecycleObserver(song: SimpleSong?, state: PlayerState, errorMsg: String) {
        var consumer: ((AudioLifecycleObserver) -> Unit)? = null
        when (state) {
            PlayerState.PREPARING -> consumer = {
                val currentPlayerInfo = getCurrentPlayerInfo()
                currentPlayerInfo?.run {
                    it.onPrepare(
                        song!!,
                        curSongIndex,
                        curPlayList.size
                    )
                }
            }
            PlayerState.PLAYING, PlayerState.RESUMED -> consumer = {
                it.onPlaying(song!!)
            }
            PlayerState.PAUSED -> consumer = {
                it.onAudioPause()
            }
            PlayerState.STOPPED -> consumer = {
                it.onAudioStop()
            }
            PlayerState.COMPLETED -> consumer = {
                it.onCompleted()
            }
            PlayerState.FINISHED -> consumer = {
                it.onFinished()
            }
            PlayerState.ERROR -> consumer = {
                it.onError(errorMsg)
            }
            else -> {}
        }
        lifecycleObservers.forEach {
            consumer?.invoke(it)
        }
    }

    private fun notifyProgressObserver(curPosition: Long, duration: Long) {
        progressObservers.forEach {
            it.onProgressUpdated(curPosition, duration)
        }
    }

}