package com.frankhon.fantasymusic.media

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import com.frankhon.fantasymusic.Fantasy
import com.frankhon.fantasymusic.utils.Constants
import com.frankhon.fantasymusic.utils.ToastUtil.showToast
import com.frankhon.fantasymusic.utils.getSystemService
import com.hon.mylogger.MyLogger
import java.io.IOException
import java.util.function.Consumer

/**
 * Created by Frank_Hon on 3/11/2019.
 * E-mail: v-shhong@microsoft.com
 */
object AudioPlayer {
    private val mMediaPlayer: MediaPlayer = MediaPlayer().apply {
        setOnErrorListener { mediaPlayer, what, extra ->
            onError(
                mediaPlayer,
                what,
                extra
            )
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
                if (mPlayerState == State.TRANSIENT_PAUSED) {
                    resume()
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                release()
            }
        }
    private var mHttpProxyCache: HttpProxyCache? = null
    private val observers = mutableListOf<AudioLifecycleObserver>()
    private var mPlayerState = State.IDLE

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

    @Throws(IOException::class)
    fun getDuration(audioFilePath: String?): Int {
        mMediaPlayer.setDataSource(audioFilePath)
        mMediaPlayer.prepare()
        val duration = mMediaPlayer.duration / 1000
        mMediaPlayer.reset()
        return duration
    }

    @JvmStatic
    fun play(audioFilePath: String) {
        prepare(audioFilePath)
    }

    @JvmStatic
    fun pause() {
        if (mMediaPlayer.isPlaying) {
            mMediaPlayer.pause()
            mPlayerState = State.PAUSED
            //            abandonAudioFocus();
            sendState(State.PAUSED.ordinal)
        }
    }

    @JvmStatic
    fun resume() {
        when (mPlayerState) {
            State.PAUSED, State.TRANSIENT_PAUSED, State.COMPLETED -> {
                val result = requestAudioFocus()
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mMediaPlayer.start()
                    mPlayerState = State.RESUMED
                    sendState(State.RESUMED.ordinal)
                }
            }
            else -> {}
        }
    }

    private fun prepare(audioFilePath: String) {
        mPlayerState = State.PREPARING
        notifyLifecycleObserver(mPlayerState)
        val result = requestAudioFocus()
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            try {
                mMediaPlayer.reset()
                if (audioFilePath.startsWith("file://")) {
                    mMediaPlayer.setDataSource(audioFilePath)
                } else {
                    if (mHttpProxyCache == null) {
                        mHttpProxyCache = HttpProxyCache.getInstance()
                    }
                    mMediaPlayer.setDataSource(mHttpProxyCache!!.getProxyUrl(audioFilePath))
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

    private fun onPrepared(mediaPlayer: MediaPlayer) {
        mediaPlayer.start()
        mPlayerState = State.PLAYING
        notifyLifecycleObserver(mPlayerState)
    }

    private fun onCompleted(mediaPlayer: MediaPlayer) {
        mPlayerState = State.COMPLETED
        sendState(State.COMPLETED.ordinal)
        notifyLifecycleObserver(mPlayerState)
    }

    private fun onError(mediaPlayer: MediaPlayer, what: Int, extra: Int): Boolean {
        mediaPlayer.reset()
        return false
    }

    private fun transientPause() {
        if (mMediaPlayer.isPlaying) {
            mMediaPlayer.pause()
            mPlayerState = State.TRANSIENT_PAUSED
            sendState(State.TRANSIENT_PAUSED.ordinal)
        }
    }

    fun release() {
        mMediaPlayer.release()
        abandonAudioFocus()
        if (mHttpProxyCache != null) {
            mHttpProxyCache!!.shutdown()
        }
    }

    private fun sendState(state: Int) {
        val intent = Intent(Constants.MUSIC_INFO_ACTION)
        intent.putExtra(Constants.KEY_PLAYER_STATE, state)
        Fantasy.getAppContext().sendBroadcast(intent)
    }

    private fun notifyLifecycleObserver(state: State) {
        var consumer: ((AudioLifecycleObserver) -> Unit)? = null
        when (state) {
            State.PREPARING -> consumer = {
                it.onPrepare()
            }
            State.PLAYING -> consumer = {
                it.onPlaying()
            }
            State.PAUSED -> consumer = {
                it.onPause()
            }
            State.RESUMED -> consumer = {
                it.onResume()
            }
            State.COMPLETED -> consumer = {
                it.onCompleted()
            }
            else -> {}
        }
        for (observer in observers) {
            consumer?.invoke(observer)
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

    private enum class State {
        IDLE,
        PREPARING,
        PLAYING,
        RESUMED,
        TRANSIENT_PAUSED,  // passive pause
        PAUSED,
        COMPLETED
    }

}