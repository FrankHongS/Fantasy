package com.frankhon.fantasymusic.media

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioManager.*
import android.media.MediaPlayer
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import com.frankhon.fantasymusic.utils.*
import com.frankhon.fantasymusic.vo.CurrentPlayerInfo
import com.frankhon.fantasymusic.vo.SimpleSong
import com.hon.mylogger.MyLogger
import kotlinx.coroutines.*

/**
 * Note:
 * 1. MediaPlayerNative: error (-38, 0)
 * 当`MediaPlayer`处于uninitialized和preparing(该状态即：还在准备当中，未完成准备)状态时,调用
 * `getDuration()`或者`getCurrentPosition()`，native将报错 error (-38, 0)
 *
 * Created by Frank_Hon on 3/11/2019.
 * E-mail: v-shhong@microsoft.com
 */
object AudioPlayer {
    private val mediaPlayer by lazy {
        MediaPlayer().apply {
            setOnErrorListener { mediaPlayer, what, extra ->
                onError(mediaPlayer, what, extra)
            }
            setOnCompletionListener { mediaPlayer -> onCompleted(mediaPlayer) }
            setOnPreparedListener { mediaPlayer -> onPrepared(mediaPlayer) }
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setLegacyStreamType(STREAM_MUSIC)
                    .build()
            )
        }
    }
    private val audioManager = getSystemService<AudioManager>(Context.AUDIO_SERVICE)
    private val onAudioFocusChangeListener: OnAudioFocusChangeListener =
        OnAudioFocusChangeListener { focusChange: Int ->
            MyLogger.d("focusChange: $focusChange")
            when (focusChange) {
                AUDIOFOCUS_LOSS_TRANSIENT -> {
                    if (curState == PlayerState.PLAYING || curState == PlayerState.RESUMED) {
                        transientPause()
                    }
                }
                AUDIOFOCUS_GAIN -> {
                    if (isTransientPause) {
                        isTransientPause = false
                        resume()
                    }
                }
                AUDIOFOCUS_LOSS -> pause()
            }
        }
    private val audioFocusRequest by lazy {
        AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(onAudioFocusChangeListener)
            .setAudioAttributes(
                AudioAttributesCompat.Builder()
                    .setLegacyStreamType(STREAM_MUSIC)
                    .build()
            )
            .build()
    }
    private val mHttpProxyCache by lazy { HttpProxyCache.getInstance() }

    //是否为其他应用占用焦点，短暂暂停
    private var isTransientPause = false

    private var curSong: SimpleSong? = null
    private var curIndex = -1
    private var curState = PlayerState.IDLE
    private val curPlaylist by lazy { mutableListOf<SimpleSong>() }
    private var curPlayMode = PlayMode.LOOP_LIST
    private var errorMsg = ""

    /**
     * 原始播放列表，即顺序播放列表，[PlayMode.LOOP_LIST]和[PlayMode.LOOP_SINGLE]都需使用该序列
     */
    private val originPlaylist by lazy { mutableListOf<SimpleSong>() }

    private val mainScope by lazy { MainScope() }
    private var monitorProgressJob: Job? = null

    //region 播放器对外暴露的方法
    @JvmStatic
    fun play(song: SimpleSong?) {
        song?.let {
            val index = curPlaylist.indexOf(song)
            if (index != -1) {
                play(index)
            }
        }
    }

    @JvmStatic
    fun playAndAddIntoPlaylist(song: SimpleSong?) {
        song?.let {
            val index = curPlaylist.indexOf(it)
            if (index == -1) {
                originPlaylist.add(0, it)
                curPlaylist.add(curIndex + 1, it)
                play(curIndex + 1)
            } else {
                play(index)
            }
        }
    }

    @JvmStatic
    fun addIntoPlaylist(song: SimpleSong): Boolean {
        song.let {
            if (!curPlaylist.contains(it)) {
                if (curPlaylist.isEmpty()) {
                    playAndAddIntoPlaylist(it)
                    return true
                } else {
                    originPlaylist.add(0, it)
                    curPlaylist.add(curIndex + 1, it)
                }
            }
        }
        return false
    }

    @JvmStatic
    fun removeSongFromPlayList(index: Int) {
        originPlaylist.remove(curPlaylist[index])
        if (index == curIndex) {
            if (curPlaylist.size > 1) {
                next()
            }
            curPlaylist.removeAt(index)
        } else {
            curPlaylist.removeAt(index)
        }
        curIndex = curPlaylist.indexOf(curSong)
        updatePlayerConfig(PlayerConfiguration.PLAYLIST)
    }

    @JvmStatic
    fun setPlaylist(playlist: List<SimpleSong>, index: Int) {
        if (index >= playlist.size) {
            MyLogger.e("setPlayList: index = $index is out of range, playlist's size = ${playlist.size}")
            return
        }
        originPlaylist.setData(playlist)
        when (curPlayMode) {
            PlayMode.LOOP_LIST, PlayMode.LOOP_SINGLE -> {
                curPlaylist.setData(playlist)
                play(index)
            }
            PlayMode.SHUFFLE -> {
                val song = playlist[index]
                curPlaylist.setData(playlist.shuffled())
                val newIndex = curPlaylist.indexOf(song)
                play(newIndex)
            }
        }
        MyLogger.d("setPlaylist: playMode = $curPlayMode, playlist = $curPlaylist")
    }

    @JvmStatic
    fun pause() {
        if (mediaPlayer.isPlaying) {
            MyLogger.d("prepare() playerState = ${PlayerState.PAUSED}")
            mediaPlayer.pause()
            updatePlayerState(PlayerState.PAUSED)
        }
    }

    @JvmStatic
    fun resume() {
        if (!mediaPlayer.isPlaying) {
            val result = requestAudioFocus()
            if (result == AUDIOFOCUS_REQUEST_GRANTED) {
                mediaPlayer.start()
                MyLogger.d("resume() playerState = ${PlayerState.RESUMED}")
                updatePlayerState(PlayerState.RESUMED)
            } else {
                MyLogger.e("Error to resume playing: $result")
                showToast("恢复播放失败")
            }
        }
    }

    @JvmStatic
    fun stop() {
        try {
            if (curState != PlayerState.IDLE &&
                curState != PlayerState.PREPARING
            ) {
                mediaPlayer.stop()
                updatePlayerState(PlayerState.STOPPED)
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun seekTo(msec: Int) {
        mediaPlayer.seekTo(msec)
    }

    @JvmStatic
    fun previous() {
        if (curState != PlayerState.PREPARING) {
            play(curIndex - 1)
        }
    }

    @JvmStatic
    fun next() {
        val success = play(curIndex + 1)
        if (!success) {
            updatePlayerState(PlayerState.FINISHED)
        }
    }

    @JvmStatic
    fun setPlayMode(playMode: String) {
        curPlayMode = PlayMode.valueOf(playMode)
        mediaPlayer.isLooping = curPlayMode == PlayMode.LOOP_SINGLE
        if (curPlayMode == PlayMode.SHUFFLE) {
            curPlaylist.shuffle()
        } else {
            curPlaylist.setData(originPlaylist)
        }
        curIndex = curPlaylist.indexOf(curSong)
        updatePlayerConfig(PlayerConfiguration.PLAY_MODE)
        MyLogger.d("setPlayMode: playMode = $curPlayMode, playlist = $curPlaylist")
    }

    @JvmStatic
    fun getCurrentPlayerInfo(): CurrentPlayerInfo {
        return CurrentPlayerInfo().also {
            it.curSong = curSong
            it.curPlaylist = curPlaylist
            it.curSongIndex = curIndex
            it.curPlayerState = curState
            it.curPlaybackPosition = getCurrentPosition()
            it.curPlayMode = curPlayMode
        }
    }

    // endregion

    private fun getDuration(): Long {
        val duration = if (curState == PlayerState.IDLE || curState == PlayerState.PREPARING) {
            0
        } else {
            mediaPlayer.duration
        }
        return duration.toLong()
    }

    private fun getCurrentPosition(): Long {
        val currentPosition =
            if (curState == PlayerState.IDLE || curState == PlayerState.PREPARING) {
                0
            } else {
                mediaPlayer.currentPosition
            }
        return currentPosition.toLong()
    }

    private fun play(index: Int): Boolean {
        if (curPlaylist.isEmpty()) {
            return false
        }
        return if (index >= 0 && index < curPlaylist.size) {
            innerPlay(index)
            true
        } else if (index == curPlaylist.size) {
            innerPlay(index % curPlaylist.size)
            true
        } else if (index == -1) {
            innerPlay(curPlaylist.size - 1)
            true
        } else {
            MyLogger.d("index = $index is out of range, playList's size = ${curPlaylist.size}")
            false
        }
    }

    private fun innerPlay(index: Int) {
        curIndex = index
        curSong = curPlaylist[curIndex]
        prepare(curSong?.songUri.orEmpty())
    }

    private fun prepare(audioFilePath: String) {
        updatePlayerState(PlayerState.PREPARING)
        MyLogger.d("prepare() playerState = ${PlayerState.PREPARING}")
        val result = requestAudioFocus()
        if (result == AUDIOFOCUS_REQUEST_GRANTED) {
            try {
                mediaPlayer.reset()
                if (audioFilePath.startsWith("file://")) {
                    mediaPlayer.setDataSource(audioFilePath)
                } else {
                    mediaPlayer.setDataSource(mHttpProxyCache.getProxyUrl(audioFilePath))
                }
                mediaPlayer.isLooping = curPlayMode == PlayMode.LOOP_SINGLE
                mediaPlayer.prepareAsync()
            } catch (e: Exception) {
                e.printStackTrace()
                MyLogger.e("Error to play: $e")
            }
        } else {
            MyLogger.e("Error to request playing: $result")
            showToast("请求播放失败")
        }
    }

    private fun onPrepared(player: MediaPlayer) {
        player.start()
        updatePlayerState(PlayerState.PLAYING)
        MyLogger.d("onPrepared() playerState = ${PlayerState.PLAYING}, curSong = $curSong")
    }

    private fun startUpdateProgress() {
        launchProgressMonitor()
    }

    private fun stopUpdateProgress() {
        if (curState == PlayerState.COMPLETED) {
            updateProgress()
        }
        monitorProgressJob?.cancel()
    }

    private fun launchProgressMonitor() {
        monitorProgressJob?.cancel()
        monitorProgressJob = mainScope.launch(SupervisorJob()) {
            //死循环会堵塞主线程，所以此处用IO线程
            withContext(Dispatchers.IO) {
                while (true) {
                    updateProgress()
                    delay(700)
                }
            }
        }
    }

    private fun onCompleted(mp: MediaPlayer) {
        MyLogger.d("onCompleted() playerState = ${PlayerState.COMPLETED} isPlaying=${mp.isPlaying}")
        updatePlayerState(PlayerState.COMPLETED)
        if (curPlayMode != PlayMode.LOOP_SINGLE) {
            next()
        }
    }

    /**
     * @return true, consume the error, and won't invoke onCompletion()
     */
    private fun onError(mediaPlayer: MediaPlayer, what: Int, extra: Int): Boolean {
        MyLogger.d("onError() playerState = ${PlayerState.ERROR}, what = $what, extra = $extra")
        this.errorMsg = "AudioPlayer: Error($what, $extra)"
        mediaPlayer.reset()
        updatePlayerState(PlayerState.ERROR)
        return true
    }

    private fun transientPause() {
        MyLogger.d("transientPause()")
        isTransientPause = true
        pause()
    }

    @JvmStatic
    fun release() {
        MyLogger.d("release()")
        stopUpdateProgress()
        abandonAudioFocus()
        stop()
        mHttpProxyCache.shutdown()
        resetCurPlayInfo()
    }

    private fun resetCurPlayInfo() {
        curSong = null
        curPlaylist.clear()
        originPlaylist.clear()
        curIndex = -1
        curState = PlayerState.IDLE
        curPlayMode = PlayMode.LOOP_LIST
    }

    private fun updatePlayerState(state: PlayerState) {
        this.curState = state
        when (state) {
            PlayerState.PLAYING, PlayerState.RESUMED -> {
                startUpdateProgress()
                curSong?.duration = getDuration()
            }
            else -> stopUpdateProgress()
        }
        sendState()
    }

    private fun requestAudioFocus(): Int {
        return AudioManagerCompat.requestAudioFocus(audioManager, audioFocusRequest)
    }

    private fun abandonAudioFocus(): Int {
        return AudioManagerCompat.abandonAudioFocusRequest(audioManager, audioFocusRequest)
    }

    private fun sendState() {
        if (curState != PlayerState.STOPPED) {
            sendMediaNotification(
                getCurrentPlayerInfo(),
                curState != PlayerState.IDLE
                        && curState != PlayerState.ERROR
                        && curState != PlayerState.PAUSED
                        && curState != PlayerState.FINISHED
            )
        } else {
            cancelNotification()
        }
        curSong?.let {
            sendBroadcast(
                Intent(MUSIC_INFO_ACTION).apply {
                    //在Android 8.0 以上要求静态注册的BroadcastReceiver所接收的消息必须是显式的，
                    // 我们通过设置包名的方式来告诉系统这个Intent是要发给哪个应用来接收。不设置的话就会接收不到消息
                    setPackage(PACKAGE_ID)
                    putExtra(KEY_PLAYER_STATE, curState.name)
                    putExtra(KEY_CUR_SONG, it)
                    if (curState == PlayerState.ERROR && errorMsg.isNotEmpty()) {
                        putExtra(KEY_PLAYER_ERROR_MESSAGE, errorMsg)
                    }
                })
        }
    }

    private fun updateProgress() {
        sendBroadcast(
            Intent(MUSIC_PROGRESS_ACTION).apply {
                setPackage(PACKAGE_ID)
                putExtra(KEY_SONG_PROGRESS, getCurrentPosition())
                putExtra(KEY_DURATION, getDuration())
            })
    }

    private fun updatePlayerConfig(configType: PlayerConfiguration) {
        sendBroadcast(
            Intent(MUSIC_PLAY_MODE_ACTION).apply {
                setPackage(PACKAGE_ID)
                if (configType == PlayerConfiguration.PLAY_MODE) {
                    putExtra(KEY_PLAY_MODE, curPlayMode.name)
                }
                putExtra(KEY_PLAYER_CONFIG, configType.name)
            })
    }
}