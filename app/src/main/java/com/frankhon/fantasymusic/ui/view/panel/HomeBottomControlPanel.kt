package com.frankhon.fantasymusic.ui.view.panel

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.media.*
import com.frankhon.fantasymusic.media.observer.PlayerConfigurationObserver
import com.frankhon.fantasymusic.media.observer.PlayerLifecycleObserver
import com.frankhon.fantasymusic.ui.activities.SongDetailActivity
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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

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

    private val mainScope by lazy { MainScope() }

    init {
        View.inflate(context, R.layout.layout_panel, this)

        setBackgroundColor(context.color(R.color.navigationBarColor))
        ViewCompat.setElevation(this, 8.dp.toFloat())
        setDefaultPanel()

        ib_player_toggle.bindClickListener()

        ib_next_song.setOnClickListener {
            AudioPlayerManager.next()
        }
        ib_previous_song.setOnClickListener {
            AudioPlayerManager.previous()
        }

        ib_play_mode.bindClickListener()
        sb_play_progress.bindChangeListener()

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

        view_panel_mask.setOnClickListener {
            AudioPlayerManager.getCurrentPlayerInfo()?.takeIf { !it.curPlayerState.isStopped() }
                ?.run {
                    context.navigateWithTransitions<SongDetailActivity>()
                }
        }
    }

    fun connectAudioPlayer() {
        MyLogger.d("connectAudioPlayer: ")
        AudioPlayerManager.connect(object : AudioPlayerManager.OnServiceConnectedListener {
            override fun onServiceConnected(manager: AudioPlayerManager) {
                manager.registerLifecycleObserver(this@HomeBottomControlPanel)
                manager.registerConfigurationObserver(this@HomeBottomControlPanel)
            }
        })
    }

    fun disconnectAudioPlayer() {
        MyLogger.d("disconnectAudioPlayer: ")
        AudioPlayerManager.let {
            it.unregisterLifecycleObserver(this)
            it.unregisterConfigurationObserver(this)
        }
    }

    override fun onDetachedFromWindow() {
        MyLogger.d("onDetachedFromWindow: ")
        super.onDetachedFromWindow()
        ib_playlist.dismissPlaylistPopup()
        mainScope.cancel()
    }

    //region Audio lifecycle
    override fun onPlayerConnected(playerInfo: CurrentPlayerInfo?) {
        playerInfo?.run {
            MyLogger.d("onPlayerConnected: curPlayerState = $curPlayerState")
            if (curPlayerState.isStopped()) {
                setDefaultPanel()
                return
            }
            curSong?.let {
                updateSongPanel(it)
                ib_player_toggle.updatePlayControlIcon(curPlayerState, false)
                updatePreviousNextButton(curPlayMode, curSongIndex, curPlaylist.size)
                ib_play_mode.playMode = PlayModeImageButton.State.valueOf(curPlayMode.name)
                // update progress
                updateSongDuration(it)
                tv_current_time.text = msToMMSS(curPlaybackPosition)
                sb_play_progress.progress = curPlaybackPosition.toInt()
                iv_song_bottom_pic.updateProgress(
                    curPlaybackPosition.toInt(),
                    it.duration.toInt()
                )
                // update lyrics
                loadLyrics(it) {
                    LyricsManager.getLyricText(curPlaybackPosition).let { content ->
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
            }
        }
    }

    override fun onPrepare(song: SimpleSong, playMode: PlayMode, curIndex: Int, totalSize: Int) {
        tv_song_lyrics.isVisible = false
        iv_song_bottom_pic.cancelRotateAnimator()
        updateSongPanel(song)
        ib_player_toggle.updatePlayControlIcon(PlayerState.PREPARING)
        updatePreviousNextButton(playMode, curIndex, totalSize)
        //更新播放列表中当前播放歌曲
        ib_playlist.updatePlaylistPopup(index = curIndex)
        loadLyrics(song)
    }

    override fun onPlaying(song: SimpleSong) {
        updateSongDuration(song)
        ib_player_toggle.updatePlayControlIcon(PlayerState.PLAYING)
        iv_song_bottom_pic.startRotateAnimator()
        tv_bottom_song_name.isSelected = true
    }

    override fun onAudioResume(song: SimpleSong) {
        ib_player_toggle.updatePlayControlIcon(PlayerState.RESUMED)
        iv_song_bottom_pic.resumeRotateAnimator()
        tv_bottom_song_name.isSelected = true
    }

    override fun onAudioPause() {
        ib_player_toggle.updatePlayControlIcon(PlayerState.PAUSED)
        iv_song_bottom_pic.pauseRotateAnimator()
        tv_bottom_song_name.isSelected = false
    }

    override fun onAudioStop() {
        setDefaultPanel()
    }

    override fun onFinished() {
        ib_player_toggle.updatePlayControlIcon(PlayerState.FINISHED)
    }

    override fun onError(errorMsg: String) {
        ib_player_toggle.updatePlayControlIcon(PlayerState.PAUSED)
        showToast(errorMsg)
    }
    //endregion

    //region Audio player configuration
    override fun onProgressUpdated(curPosition: Long, duration: Long) {
        iv_song_bottom_pic.updateProgress(curPosition.toInt(), duration.toInt())
        tv_current_time.text = msToMMSS(curPosition)
        sb_play_progress.run {
            //未拖拽时更新进度条
            if (!isTracking) {
                progress = curPosition.toInt()
            }
        }
        LyricsManager.run {
            getLyricText(curPosition).let { tv_song_lyrics.safeSetText(it) }
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

    private fun setDefaultPanel() {
        iv_song_bottom_pic.reset(R.drawable.default_placeholder)
        tv_current_time.text = msToMMSS(0)
        tv_song_duration.text = msToMMSS(0)
        tv_bottom_song_name.run {
            text = string(R.string.app_name)
            isSelected = false
        }
        tv_bottom_artist_name.text = string(R.string.welcome_text)
        ib_player_toggle.setPlayState(AnimatedAudioToggleButton.ControlButtonState.INITIAL)
        ib_previous_song.isEnabled = false
        ib_next_song.isEnabled = false
        sb_play_progress.progress = 0
        ib_play_mode.reset()
        tv_song_lyrics.isVisible = false
    }

    private fun updateSongPanel(song: SimpleSong) {
        song.run {
            (context as? Activity)?.takeIf { !it.isDestroyed && !it.isFinishing }?.let {
                // Glide可以感知Activity的生命周期，onStop停止加载，onStart恢复加载
                Glide.with(it)
                    .load(picUrl)
                    .placeholder(R.drawable.default_placeholder)
                    .error(R.drawable.default_placeholder)
                    .into(iv_song_bottom_pic)
                tv_bottom_song_name.text = name
                tv_bottom_artist_name.text = artist
            } ?: kotlin.run {
                MyLogger.e(
                    "updateSongPanel: context=$context," +
                            " isDestroyed=${(context as? Activity)?.isDestroyed}," +
                            " isFinishing=${(context as? Activity)?.isFinishing}"
                )
            }
        }
    }

    private fun updatePreviousNextButton(curPlayMode: PlayMode, curIndex: Int, totalSize: Int) {
        if (totalSize == 0 || totalSize == 1) {
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

    private fun loadLyrics(song: SimpleSong, then: (() -> Unit)? = null) {
        mainScope.launch {
            LyricsManager.loadLyrics(song)
            then?.invoke()
        }
    }
}