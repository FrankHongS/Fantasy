package com.frankhon.fantasymusic.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.frankhon.fantasymusic.IMusicPlayer
import com.frankhon.fantasymusic.application.Fantasy
import com.frankhon.fantasymusic.media.observer.PlayerLifecycleObserver
import com.frankhon.fantasymusic.media.observer.PlayerConfigurationObserver
import com.frankhon.fantasymusic.utils.appContext
import com.frankhon.fantasymusic.utils.bindService
import com.frankhon.fantasymusic.utils.startService
import com.frankhon.fantasymusic.vo.SimpleSong

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

    private val lifecycleObservers = mutableListOf<PlayerLifecycleObserver>()
    private val configurationObservers = mutableListOf<PlayerConfigurationObserver>()

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
            val intent = Intent(appContext, AudioPlayerService::class.java)
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
            startService(intent)
        }
    }

    private fun invokeOnServiceConnected() {
        onServiceConnectedListener?.invoke(this)
        // avoid memory leak from anonymous inner class
        onServiceConnectedListener = null
    }

    @JvmStatic
    fun registerLifecycleObserver(observer: PlayerLifecycleObserver) {
        if (!lifecycleObservers.contains(observer)) {
            lifecycleObservers.add(observer)
            observer.onPlayerConnected(getCurrentPlayerInfo())
        }
    }

    @JvmStatic
    fun unregisterLifecycleObserver(observer: PlayerLifecycleObserver) {
        lifecycleObservers.remove(observer)
    }

    @JvmStatic
    fun registerProgressObserver(observer: PlayerConfigurationObserver) {
        if (!configurationObservers.contains(observer)) {
            configurationObservers.add(observer)
        }
    }

    @JvmStatic
    fun unregisterProgressObserver(observer: PlayerConfigurationObserver) {
        configurationObservers.remove(observer)
    }

    @JvmStatic
    fun release() {
        Fantasy.getAppContext().unbindService(connection)
        hasBoundService = false
        lifecycleObservers.clear()
        configurationObservers.clear()
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
    fun play(index: Int) {
        val currentPlayerInfo = getCurrentPlayerInfo()
        currentPlayerInfo?.run {
            if (index != curSongIndex && index >= 0 && index < curPlaylist.size) {
                play(curPlaylist[index])
            }
        }
    }

    @JvmStatic
    fun playAndAddIntoPlaylist(song: SimpleSong?) {
        musicPlayer?.playAndAddIntoPlaylist(song)
    }

    @JvmStatic
    fun addIntoPlaylist(song: SimpleSong) {
        musicPlayer?.addIntoPlaylist(song)
    }

    @JvmStatic
    fun removeSongFromPlayList(index: Int) {
        musicPlayer?.removeSongFromPlayList(index)
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
    fun setPlayMode(playMode: PlayMode) {
        musicPlayer?.setPlayMode(playMode.name)
    }

    @JvmStatic
    fun getCurrentPlayerInfo() = musicPlayer?.currentPlayerInfo

    fun publishPlayerState(song: SimpleSong?, state: PlayerState, errorMsg: String) {
        notifyLifecycleObserver(song, state, errorMsg)
    }

    fun publishPlayerProgress(curPosition: Long, duration: Long) {
        notifyProgressObserver(curPosition, duration)
    }

    fun publishPlayMode(playMode: String) {
        notifyPlayModeObserver(PlayMode.valueOf(playMode))
    }

    fun publishPlaylistChanged() {
        notifyPlaylistObserver()
    }

    private fun notifyLifecycleObserver(song: SimpleSong?, state: PlayerState, errorMsg: String) {
        var consumer: ((PlayerLifecycleObserver) -> Unit)? = null
        when (state) {
            PlayerState.PREPARING -> consumer = {
                val currentPlayerInfo = getCurrentPlayerInfo()
                currentPlayerInfo?.run {
                    it.onPrepare(
                        song!!,
                        curPlayMode,
                        curSongIndex,
                        curPlaylist.size
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
        configurationObservers.forEach {
            it.onProgressUpdated(curPosition, duration)
        }
    }

    private fun notifyPlayModeObserver(playMode: PlayMode) {
        val currentPlayerInfo = getCurrentPlayerInfo()
        currentPlayerInfo?.run {
            configurationObservers.forEach {
                it.onPlayModeChanged(playMode, curSongIndex, curPlaylist.size)
            }
        }
    }

    private fun notifyPlaylistObserver() {
        val currentPlayerInfo = getCurrentPlayerInfo()
        currentPlayerInfo?.run {
            configurationObservers.forEach {
                it.onPlaylistChanged(curPlayMode, curPlaylist, curSongIndex)
            }
        }
    }

}