package com.frankhon.fantasymusic.ui.view.panel

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.media.*
import com.frankhon.fantasymusic.media.observer.PlayerConfigurationObserver
import com.frankhon.fantasymusic.media.observer.PlayerLifecycleObserver
import com.frankhon.fantasymusic.ui.view.AnimatedAudioToggleButton
import com.frankhon.fantasymusic.ui.view.PlayModeImageButton
import com.frankhon.fantasymusic.utils.*
import com.frankhon.fantasymusic.utils.popup.dismissPlaylistPopup
import com.frankhon.fantasymusic.utils.popup.showPlaylistPopup
import com.frankhon.fantasymusic.utils.popup.showSchedulePopup
import com.frankhon.fantasymusic.utils.popup.updatePlaylistPopup
import com.frankhon.fantasymusic.vo.CurrentPlayerInfo
import com.frankhon.fantasymusic.vo.SimpleSong
import com.hon.mylogger.MyLogger
import kotlinx.android.synthetic.main.layout_panel.view.*
import kotlinx.android.synthetic.main.layout_song_control.view.*

/**
 * 相对复杂的底部控制栏
 *
 * Created by Frank Hon on 2022/11/10 5:41 下午.
 * E-mail: frank_hon@foxmail.com
 */
class HomeBottomControlPanel @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), PlayerLifecycleObserver,
    PlayerConfigurationObserver {

    init {
        View.inflate(context, R.layout.layout_panel, this)

        setBackgroundColor(context.color(R.color.navigationBarColor))
        ViewCompat.setElevation(this, 8.dp.toFloat())
        setDefaultPanel()

        ib_player_toggle.setOnControlButtonClickListener { curState ->
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
        ib_next_song.setOnClickListener {
            AudioPlayerManager.next()
        }
        ib_previous_song.setOnClickListener {
            AudioPlayerManager.previous()
        }
        ib_play_mode.setPlayModeListener {
            val toastText = when (it) {
                PlayModeImageButton.State.SHUFFLE -> {
                    AudioPlayerManager.setPlayMode(PlayMode.SHUFFLE)
                    getString(R.string.play_mode_shuffle)
                }
                PlayModeImageButton.State.LOOP_SINGLE -> {
                    AudioPlayerManager.setPlayMode(PlayMode.LOOP_SINGLE)
                    getString(R.string.play_mode_single_loop)
                }
                PlayModeImageButton.State.LOOP_LIST -> {
                    AudioPlayerManager.setPlayMode(PlayMode.LOOP_LIST)
                    getString(R.string.play_mode_list_loop)
                }
            }
            showToast(toastText)
        }
        sb_play_progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
        ib_playlist.setOnClickListener {
            val currentPlayerInfo = AudioPlayerManager.getCurrentPlayerInfo()
            currentPlayerInfo?.run {
                it.showPlaylistPopup(curPlaylist.transferToSongItems(curSongIndex),
                    hOffset = it.width / 2, {
                        AudioPlayerManager.removeSongFromPlayList(it)
                    }) {
                    AudioPlayerManager.play(it)
                }
            }
        }
        ib_schedule_pause.setOnClickListener {
            it.showSchedulePopup()
        }
    }

    override fun onAttachedToWindow() {
        MyLogger.d("onAttachedToWindow: ")
        super.onAttachedToWindow()
        AudioPlayerManager.connect {
            it.registerLifecycleObserver(this)
            it.registerConfigurationObserver(this)
        }
    }

    override fun onDetachedFromWindow() {
        MyLogger.d("onDetachedFromWindow: ")
        super.onDetachedFromWindow()
        ib_playlist.dismissPlaylistPopup()
        AudioPlayerManager.let {
            it.unregisterLifecycleObserver(this)
            it.unregisterConfigurationObserver(this)
        }
    }

    //region Audio lifecycle
    override fun onPlayerConnected(playerInfo: CurrentPlayerInfo?) {
        playerInfo?.run {
            MyLogger.d("onPlayerConnected: curPlayerState = $curPlayerState")
            curSong?.let {
                updateSongPanel(it)
                updatePlayControlIcon(curPlayerState, false)
                updatePreviousNextButton(curPlayMode, curSongIndex, curPlaylist.size)
                ib_play_mode.playMode = PlayModeImageButton.State.valueOf(curPlayMode.name)
                updateSongDuration(it)
                // update progress
                tv_current_time.text = msToMMSS(curPlaybackPosition)
                sb_play_progress.progress = curPlaybackPosition.toInt()
                LyricsManager.run {
                    compareAndGetLyric(curPlaybackPosition)?.let { content ->
                        tv_song_lyrics.safeSetText(content)
                    }
                }
                // update album image
                if (curPlayerState.isPlaying()) {
                    tv_bottom_song_name.isSelected = true
                    iv_song_bottom_pic.startRotateAnimator()
                } else {
                    tv_bottom_song_name.isSelected = false
                    iv_song_bottom_pic.cancelRotateAnimator()
                }
                iv_song_bottom_pic.startUpdateProgress(
                    curPlaybackPosition.toInt(),
                    it.duration.toInt()
                )
            }
        }
    }

    override fun onPrepare(song: SimpleSong, playMode: PlayMode, curIndex: Int, totalSize: Int) {
        tv_song_lyrics.isVisible = false
        iv_song_bottom_pic.pauseRotateAnimator()
        updateSongPanel(song)
        updatePlayControlIcon(PlayerState.PREPARING)
        updatePreviousNextButton(playMode, curIndex, totalSize)
        //更新播放列表中当前播放歌曲
        ib_playlist.updatePlaylistPopup(index = curIndex)
    }

    override fun onPlaying(song: SimpleSong) {
        updateSongDuration(song)
        updatePlayControlIcon(PlayerState.PLAYING)
        iv_song_bottom_pic.startRotateAnimator()
        tv_bottom_song_name.isSelected = true
    }

    override fun onAudioResume(song: SimpleSong) {
        updatePlayControlIcon(PlayerState.RESUMED)
        iv_song_bottom_pic.resumeRotateAnimator()
        tv_bottom_song_name.isSelected = true
    }

    override fun onAudioPause() {
        updatePlayControlIcon(PlayerState.PAUSED)
        iv_song_bottom_pic.pauseRotateAnimator()
        tv_bottom_song_name.isSelected = false
    }

    override fun onAudioStop() {
        setDefaultPanel()
    }

    override fun onFinished() {
        updatePlayControlIcon(PlayerState.FINISHED)
    }

    override fun onError(errorMsg: String) {
        updatePlayControlIcon(PlayerState.PAUSED)
        if (errorMsg.isNotEmpty()) {
            showToast(errorMsg)
        }
    }
    //endregion

    //region Audio player configuration
    override fun onProgressUpdated(curPosition: Long, duration: Long) {
        iv_song_bottom_pic.startUpdateProgress(curPosition.toInt(), duration.toInt())
        tv_current_time.text = msToMMSS(curPosition)
        val isTracking = (sb_play_progress.tag as? Boolean) ?: false
        //未拖拽时更新进度条
        if (!isTracking) {
            sb_play_progress.progress = curPosition.toInt()
        }
        LyricsManager.run {
            compareAndGetLyric(curPosition)?.let { tv_song_lyrics.safeSetText(it) }
        }
    }

    override fun onPlayModeChanged(playMode: PlayMode, curIndex: Int, totalSize: Int) {
        updatePreviousNextButton(playMode, curIndex, totalSize)
    }

    override fun onPlaylistChanged(playMode: PlayMode, playlist: List<SimpleSong>, curIndex: Int) {
        ib_playlist.updatePlaylistPopup(
            newPlaylist = playlist.transferToSongItems(curIndex)
        )
        updatePreviousNextButton(playMode, curIndex, playlist.size)
    }
    //endregion

    // tricky 通过通知栏切歌，然后暂停，专辑图片一直旋转；应该是属性动画在后台无法正常暂停，以下为临时处理方案
    fun doOnResume() {
        val currentPlayerInfo = AudioPlayerManager.getCurrentPlayerInfo()
        currentPlayerInfo?.run {
            if (curPlayerState == PlayerState.PAUSED) {
                iv_song_bottom_pic.pauseRotateAnimator()
            }
        }
    }

    private fun setDefaultPanel() {
        iv_song_bottom_pic.run {
            setImageResource(R.mipmap.ic_launcher)
            cancelRotateAnimator()
            resetProgress()
        }
        tv_current_time.text = msToMMSS(0)
        tv_song_duration.text = ""
        tv_bottom_song_name.run {
            text = getString(R.string.app_name)
            isSelected = false
        }
        tv_bottom_artist_name.text = getString(R.string.welcome_text)
        ib_player_toggle.setPlayState(AnimatedAudioToggleButton.ControlButtonState.INITIAL)
        ib_previous_song.isEnabled = false
        ib_next_song.isEnabled = false
        sb_play_progress.progress = 0
        tv_song_lyrics.isVisible = false
    }

    private fun updateSongPanel(song: SimpleSong) {
        song.run {
            (context as? Activity)?.takeIf { !it.isDestroyed && !it.isFinishing }?.let {
                // Glide可以感知Activity的生命周期，onStop停止加载，onStart恢复加载
                Glide.with(it)
                    .load(picUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(iv_song_bottom_pic)
                tv_bottom_song_name.text = name
                tv_bottom_artist_name.text = artist
//                panelLayout.setAllowDragging(true)
            }
        }
    }

    private fun updatePlayControlIcon(playerState: PlayerState, shouldAnimate: Boolean = true) {
        when (playerState) {
            PlayerState.PLAYING, PlayerState.RESUMED -> ib_player_toggle.setPlayState(
                AnimatedAudioToggleButton.ControlButtonState.PLAYING, shouldAnimate
            )
            PlayerState.PREPARING -> ib_player_toggle.setPlayState(
                AnimatedAudioToggleButton.ControlButtonState.PREPARING,
                shouldAnimate
            )
            else -> ib_player_toggle.setPlayState(
                AnimatedAudioToggleButton.ControlButtonState.PAUSED,
                shouldAnimate
            )
        }
    }

    private fun updatePreviousNextButton(curPlayMode: PlayMode, curIndex: Int, totalSize: Int) {
        if (totalSize == 1) {
            ib_previous_song.isEnabled = false
            ib_next_song.isEnabled = false
            return
        }
        if (curPlayMode == PlayMode.LOOP_SINGLE) {
            ib_previous_song.isEnabled = curIndex != 0
            ib_next_song.isEnabled = curIndex != totalSize - 1
        } else {
            ib_previous_song.isEnabled = true
            ib_next_song.isEnabled = true
        }
    }

    private fun updateSongDuration(song: SimpleSong) {
        song.let {
            tv_song_duration.text = msToMMSS(it.duration)
            sb_play_progress.run {
                max = it.duration.toInt()
            }
        }
    }
}