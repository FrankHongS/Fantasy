package com.frankhon.fantasymusic.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioManager.*
import android.media.MediaPlayer
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import com.frankhon.fantasymusic.utils.ToastUtil.showToast
import com.frankhon.fantasymusic.utils.getSystemService
import com.frankhon.fantasymusic.vo.SimpleSong
import com.hon.mylogger.MyLogger
import kotlinx.coroutines.*
import java.io.IOException

/**
 * Created by Frank_Hon on 3/11/2019.
 * E-mail: v-shhong@microsoft.com
 */
object AudioPlayer {
    private val mMediaPlayer: MediaPlayer = MediaPlayer().apply {
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
    private val audioManager = getSystemService<AudioManager>(Context.AUDIO_SERVICE)
    private val onAudioFocusChangeListener: OnAudioFocusChangeListener =
        OnAudioFocusChangeListener { focusChange: Int ->
            MyLogger.d("focusChange: $focusChange")
            when (focusChange) {
                AUDIOFOCUS_LOSS_TRANSIENT -> transientPause()
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
    private val observers = mutableListOf<AudioLifecycleObserver>()

    //是否为其他应用占用焦点，短暂暂停
    private var isTransientPause = false

    private lateinit var curSong: SimpleSong
    private var curIndex = -1
    private var curState = PlayerState.IDLE
    private val curPlayList by lazy { mutableListOf<SimpleSong>() }

    private val mainScope by lazy { MainScope() }
    private var monitorProgressJob: Job? = null

    @JvmStatic
    fun registerObserver(observer: AudioLifecycleObserver) {
        if (!observers.contains(observer)) {
            observers.add(observer)
        }
    }

    @JvmStatic
    fun unregisterObserver(observer: AudioLifecycleObserver) {
        observers.remove(observer)
    }

    @JvmStatic
    fun play(song: SimpleSong?) {
        song?.let {
            if (curIndex == -1) {
                curPlayList.add(it)
                play(0)
            } else {
                curPlayList.add(curIndex + 1, it)
                play(curIndex + 1)
            }
        }
    }

    @JvmStatic
    fun setPlayList(playList: List<SimpleSong>, index: Int) {
        curPlayList.clear()
        curPlayList.addAll(playList)
        play(index)
    }

    @JvmStatic
    fun pause() {
        if (mMediaPlayer.isPlaying) {
            MyLogger.d("prepare() playerState = ${PlayerState.PAUSED}")
            mMediaPlayer.pause()
            updatePlayerState(PlayerState.PAUSED)
        }
    }

    @JvmStatic
    fun resume() {
        if (!mMediaPlayer.isPlaying) {
            val result = requestAudioFocus()
            if (result == AUDIOFOCUS_REQUEST_GRANTED) {
                mMediaPlayer.start()
                MyLogger.d("resume() playerState = ${PlayerState.RESUMED}")
                updatePlayerState(PlayerState.RESUMED)
                MyLogger.d("resume() playerState = ${PlayerState.PLAYING}")
                updatePlayerState(PlayerState.PLAYING)
            }
        }
    }

    @JvmStatic
    fun seekTo(msec: Int) {
        mMediaPlayer.seekTo(msec)
    }

    @JvmStatic
    fun previous() {
        if (curState != PlayerState.PREPARING) {
            play(curIndex - 1)
        }
    }

    @JvmStatic
    fun next() {
        if (curState != PlayerState.PREPARING) {
            val success = play(curIndex + 1)
            if (!success) {
                updatePlayerState(PlayerState.FINISHED)
            }
        }
    }

    private fun play(index: Int): Boolean {
        return if (index >= 0 && index < curPlayList.size) {
            this.curSong = curPlayList[index]
            this.curIndex = index
            prepare(curSong.location.orEmpty())
            true
        } else {
            MyLogger.d("index = $index is out of range, playList's size = ${curPlayList.size}")
            false
        }
    }

    private fun prepare(audioFilePath: String) {
        updatePlayerState(PlayerState.PREPARING)
        MyLogger.d("prepare() playerState = ${PlayerState.PREPARING}")
        val result = requestAudioFocus()
        if (result == AUDIOFOCUS_REQUEST_GRANTED) {
            try {
                mMediaPlayer.reset()
                if (audioFilePath.startsWith("file://")) {
                    mMediaPlayer.setDataSource(audioFilePath)
                } else {
                    mMediaPlayer.setDataSource(mHttpProxyCache.getProxyUrl(audioFilePath))
                }
                mMediaPlayer.prepareAsync()
            } catch (e: IOException) {
                // do nothing
            }
        } else {
            MyLogger.e("Error to request playing: $result")
            showToast("请求播放失败")
        }
    }

    private fun onPrepared(player: MediaPlayer) {
        curSong.duration = player.duration.toLong()
        player.start()
        updatePlayerState(PlayerState.PLAYING)
        MyLogger.d("onPrepared() playerState = ${PlayerState.PLAYING}")
    }

    private fun startUpdateProgress() {
        launchProgressMonitor()
    }

    private fun stopUpdateProgress() {
        monitorProgressJob?.cancel()
    }

    private fun launchProgressMonitor() {
        monitorProgressJob?.cancel()
        monitorProgressJob = mainScope.launch {
            //死循环会堵塞主线程，所以此处用IO线程
            withContext(Dispatchers.IO) {
                while (true) {
                    notifyPlayerProgress()
                    delay(1000)
                }
            }
        }
    }

    private fun cancelProgressMonitor() {
        mainScope.cancel()
    }

    private fun notifyPlayerProgress() {
        observers.forEach {
            mMediaPlayer.let { player ->
                it.onProgressUpdated(player.currentPosition, player.duration)
            }
        }
    }

    private fun onCompleted(mediaPlayer: MediaPlayer) {
        MyLogger.d("onCompleted() playerState = ${PlayerState.COMPLETED}")
        updatePlayerState(PlayerState.COMPLETED)
        stopUpdateProgress()
    }

    private fun onError(mediaPlayer: MediaPlayer, what: Int, extra: Int): Boolean {
        MyLogger.d("onError() playerState = ${PlayerState.ERROR}")
        mediaPlayer.reset()
        updatePlayerState(PlayerState.ERROR)
        stopUpdateProgress()
        return false
    }

    private fun transientPause() {
        MyLogger.d("transientPause()")
        isTransientPause = true
        pause()
    }

    @JvmStatic
    fun release() {
        mMediaPlayer.release()
        abandonAudioFocus()
        mHttpProxyCache?.shutdown()
        cancelProgressMonitor()
        observers.clear()
    }

    private fun updatePlayerState(state: PlayerState) {
        this.curState = state
        notifyLifecycleObserver(state)
        when (state) {
            PlayerState.PLAYING -> startUpdateProgress()
            else -> stopUpdateProgress()
        }
    }

    private fun notifyLifecycleObserver(state: PlayerState) {
        var consumer: ((AudioLifecycleObserver) -> Unit)? = null
        when (state) {
            PlayerState.PREPARING -> consumer = {
                it.onPrepare(curSong)
            }
            PlayerState.PLAYING -> consumer = {
                it.onPlaying(curSong)
            }
            PlayerState.PAUSED -> consumer = {
                it.onPause()
            }
            PlayerState.RESUMED -> consumer = {
                it.onResume()
            }
            PlayerState.COMPLETED -> consumer = {
                it.onCompleted()
            }
            PlayerState.FINISHED -> consumer = {
                it.onFinished()
            }
            PlayerState.ERROR -> consumer = {
                it.onError()
            }
            else -> {}
        }
        observers.forEach {
            consumer?.invoke(it)
        }
    }

    private fun requestAudioFocus(): Int {
        return AudioManagerCompat.requestAudioFocus(audioManager, audioFocusRequest)
    }

    private fun abandonAudioFocus(): Int {
        return AudioManagerCompat.abandonAudioFocusRequest(audioManager, audioFocusRequest)
    }

}