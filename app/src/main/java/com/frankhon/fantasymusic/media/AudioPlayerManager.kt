package com.frankhon.fantasymusic.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.frankhon.fantasymusic.IMusicPlayer
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.application.Fantasy
import com.frankhon.fantasymusic.media.observer.PlayerConfigurationObserver
import com.frankhon.fantasymusic.media.observer.PlayerLifecycleObserver
import com.frankhon.fantasymusic.utils.appContext
import com.frankhon.fantasymusic.utils.bindService
import com.frankhon.fantasymusic.utils.showToast
import com.frankhon.fantasymusic.utils.startService
import com.frankhon.fantasymusic.vo.SimpleSong
import com.hon.mylogger.MyLogger

/**
 * Created by Frank Hon on 2020/11/1 8:26 PM.
 * E-mail: frank_hon@foxmail.com
 */
object AudioPlayerManager {
    private var musicPlayer: IMusicPlayer? = null
    private var hasBoundService = false

    //是否正在连接AudioPlayer服务；避免重复连接
    private var isConnectingService = false
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            hasBoundService = true
            isConnectingService = false
            musicPlayer = IMusicPlayer.Stub.asInterface(service)
            invokeOnServiceConnected()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            hasBoundService = false
            musicPlayer = null
        }
    }

    private val onServiceConnectedListeners = mutableListOf<OnServiceConnectedListener>()

    private val lifecycleObservers = mutableListOf<PlayerLifecycleObserver>()
    private val configurationObservers = mutableListOf<PlayerConfigurationObserver>()

    /**
     * @param listener 绑定服务之后的操作，不要在该监听器中做播放操作，因为此时不一定完成服务的绑定
     */
    @JvmStatic
    fun connect(listener: OnServiceConnectedListener? = null) {
        if (hasBoundService) {
            listener?.onServiceConnected(this)
        } else {
            if (!isConnectingService) {
                isConnectingService = true
                launchMediaService()
            }
            listener?.let {
                onServiceConnectedListeners.add(it)
            }
        }
    }

    private fun launchMediaService() {
        val intent = Intent(appContext, AudioPlayerService::class.java)
        val result = bindService(intent, connection, Context.BIND_AUTO_CREATE)
        if (!result) {
            isConnectingService = false
        }
        startService(intent)
    }

    private fun invokeOnServiceConnected() {
        LyricsManager.init()
        onServiceConnectedListeners.forEach {
            it.onServiceConnected(this)
        }
        MyLogger.d(
            "invokeOnServiceConnected: size = ${onServiceConnectedListeners.size}\n" +
                    "onServiceConnectedListeners = $onServiceConnectedListeners"
        )
        // avoid memory leak from anonymous inner class
        onServiceConnectedListeners.clear()
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
    fun registerConfigurationObserver(observer: PlayerConfigurationObserver) {
        if (!configurationObservers.contains(observer)) {
            configurationObservers.add(observer)
        }
    }

    @JvmStatic
    fun unregisterConfigurationObserver(observer: PlayerConfigurationObserver) {
        configurationObservers.remove(observer)
    }

    @JvmStatic
    fun release() {
        if (hasBoundService) {
            Fantasy.getAppContext().unbindService(connection)
            hasBoundService = false
        }
        lifecycleObservers.clear()
        configurationObservers.clear()
        LyricsManager.release()
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
        if (song?.songUri.isNullOrEmpty()) {
            showToast(R.string.play_error)
            return
        }
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

    /**
     * 播放当前歌曲，并添加到播放列表；如果播放列表已经存在，则直接播放
     * @param song
     */
    @JvmStatic
    fun playAndAddIntoPlaylist(song: SimpleSong?) {
        if (song?.songUri.isNullOrEmpty()) {
            showToast(R.string.play_error)
            return
        }
        musicPlayer?.playAndAddIntoPlaylist(song)
    }

    /**
     * 将当前歌曲添加到播放列表
     * @param song
     */
    @JvmStatic
    fun addIntoPlaylist(song: SimpleSong): Boolean {
        val currentPlayerInfo = getCurrentPlayerInfo()
        currentPlayerInfo?.run {
            val index = curPlaylist.indexOf(song)
            if (index == -1) {
                musicPlayer?.addIntoPlaylist(song)
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun removeSongFromPlayList(song: SimpleSong) {
        val currentPlayerInfo = getCurrentPlayerInfo()
        currentPlayerInfo?.run {
            val index = curPlaylist.indexOf(song)
            if (index != -1) {
                musicPlayer?.removeSongFromPlayList(index)
            }
        }
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
            PlayerState.PLAYING -> consumer = {
                it.onPlaying(song!!)
            }
            PlayerState.RESUMED -> consumer = {
                it.onAudioResume(song!!)
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

    interface OnServiceConnectedListener {

        fun onServiceConnected(manager: AudioPlayerManager)

    }

}