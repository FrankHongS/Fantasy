package com.frankhon.fantasymusic.utils

import android.widget.SeekBar
import android.widget.TextView
import androidx.core.view.isVisible
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.media.PlayMode
import com.frankhon.fantasymusic.media.PlayerState
import com.frankhon.fantasymusic.ui.view.AnimatedAudioToggleButton
import com.frankhon.fantasymusic.ui.view.PlayModeImageButton

/**
 * Created by Frank Hon on 2022/11/10 9:09 上午.
 * E-mail: frank_hon@foxmail.com
 */

fun TextView.safeSetText(content: String?) {
    if (content.isNullOrEmpty()) {
        isVisible = false
    } else {
        isVisible = true
        if (text != content) {
            text = content
        }
    }
}

fun AnimatedAudioToggleButton.bindClickListener() {
    setOnControlButtonClickListener { curState ->
        when (curState) {
            AnimatedAudioToggleButton.ControlButtonState.PLAYING -> {
                val currentPlayerInfo = AudioPlayerManager.getCurrentPlayerInfo()
                currentPlayerInfo?.run {
                    if (curPlayerState == PlayerState.ERROR) {
                        AudioPlayerManager.play(curSong)
                    } else {
                        AudioPlayerManager.resume()
                    }
                } ?: kotlin.run { AudioPlayerManager.resume() }
            }
            AnimatedAudioToggleButton.ControlButtonState.PAUSED -> AudioPlayerManager.pause()
            else -> {}
        }
    }
}

fun AnimatedAudioToggleButton.updatePlayControlIcon(
    playerState: PlayerState,
    shouldAnimate: Boolean = true
) {
    when (playerState) {
        PlayerState.PLAYING, PlayerState.RESUMED -> setPlayState(
            AnimatedAudioToggleButton.ControlButtonState.PLAYING, shouldAnimate
        )
        PlayerState.PREPARING -> setPlayState(
            AnimatedAudioToggleButton.ControlButtonState.PREPARING,
            shouldAnimate
        )
        else -> setPlayState(
            AnimatedAudioToggleButton.ControlButtonState.PAUSED,
            shouldAnimate
        )
    }
}

/**
 *  将拖拽的状态存储在tag中
 */
fun SeekBar.bindChangeListener() {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            seekBar.tag = true
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            seekBar.tag = false
            AudioPlayerManager.seekTo(seekBar.progress)
        }
    })
}

val SeekBar.isTracking
    get() = (tag as? Boolean) ?: false

fun PlayModeImageButton.bindClickListener() {
    setPlayModeListener {
        val toastText = when (it) {
            PlayModeImageButton.State.SHUFFLE -> {
                AudioPlayerManager.setPlayMode(PlayMode.SHUFFLE)
                string(R.string.play_mode_shuffle)
            }
            PlayModeImageButton.State.LOOP_SINGLE -> {
                AudioPlayerManager.setPlayMode(PlayMode.LOOP_SINGLE)
                string(R.string.play_mode_single_loop)
            }
            PlayModeImageButton.State.LOOP_LIST -> {
                AudioPlayerManager.setPlayMode(PlayMode.LOOP_LIST)
                string(R.string.play_mode_list_loop)
            }
        }
        showToast(toastText)
    }
}