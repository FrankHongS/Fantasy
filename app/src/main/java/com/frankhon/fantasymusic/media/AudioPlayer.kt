package com.frankhon.fantasymusic.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
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
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build()
        )
    }
    private val mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mOnAudioFocusChangeListener: OnAudioFocusChangeListener =
        OnAudioFocusChangeListener { focusChange: Int ->
            MyLogger.d("focusChange: $focusChange")
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                transientPause()
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                if (isTransientPause) {
                    resume()
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                release()
            }
        }
    private val mHttpProxyCache by lazy { HttpProxyCache.getInstance() }
    private val observers = mutableListOf<AudioLifecycleObserver>()

    //是否为被动暂停
    private var isTransientPause = false
    private lateinit var curSong: SimpleSong

    private lateinit var mainScope: CoroutineScope

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
    fun play(song: SimpleSong) {
        this.curSong = song
        prepare(song.location.orEmpty())
    }

    @JvmStatic
    fun pause() {
        if (mMediaPlayer.isPlaying) {
            MyLogger.d("prepare() playerState = ${PlayerState.PAUSED}")
            mMediaPlayer.pause()
            // abandonAudioFocus();
            updatePlayerState(PlayerState.PAUSED)
        }
    }

    @JvmStatic
    fun resume() {
        if (!mMediaPlayer.isPlaying) {
            val result = requestAudioFocus()
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                isTransientPause = false
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

    private fun prepare(audioFilePath: String) {
        updatePlayerState(PlayerState.PREPARING)
        MyLogger.d("prepare() playerState = ${PlayerState.PREPARING}")
        val result = requestAudioFocus()
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
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
        mainScope = MainScope().apply {
            launch {
                while (true) {
                    delay(1000)
                    notifyPlayerProgress()
                }
            }
        }
    }

    private fun notifyPlayerProgress() {
        observers.forEach {
            mMediaPlayer.let { player ->
                it.onProgressUpdated(player.currentPosition, player.duration)
            }
        }
    }

    private fun stopUpdateProgress() {
        mainScope.cancel()
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
        if (mMediaPlayer.isPlaying) {
            isTransientPause = true
            mMediaPlayer.pause()
            MyLogger.d("onError() playerState = ${PlayerState.TRANSIENT_PAUSED}")
        }
    }

    fun release() {
        mMediaPlayer.release()
        abandonAudioFocus()
        mHttpProxyCache?.shutdown()
    }

    private fun updatePlayerState(state: PlayerState) {
        notifyLifecycleObserver(state)
        when (state) {
            PlayerState.PLAYING -> startUpdateProgress()
            PlayerState.PAUSED, PlayerState.COMPLETED, PlayerState.ERROR -> stopUpdateProgress()
            else -> {}
        }
    }

    private fun notifyLifecycleObserver(state: PlayerState) {
        var consumer: ((AudioLifecycleObserver) -> Unit)? = null
        when (state) {
            PlayerState.PREPARING -> consumer = {
                it.onPrepare(curSong)
            }
            PlayerState.PLAYING -> consumer = {
                it.onPlaying()
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
        return mAudioManager.requestAudioFocus(
            mOnAudioFocusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }

    private fun abandonAudioFocus(): Int {
        return mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener)
    }

}