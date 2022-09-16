package com.frankhon.fantasymusic.activities

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.media.PlayMode
import com.frankhon.fantasymusic.media.PlayerState
import com.frankhon.fantasymusic.media.observer.PlayerConfigurationObserver
import com.frankhon.fantasymusic.media.observer.PlayerLifecycleObserver
import com.frankhon.fantasymusic.utils.*
import com.frankhon.fantasymusic.view.AnimatedAudioControlButton.ControlButtonState
import com.frankhon.fantasymusic.view.PlayModeImageButton
import com.frankhon.fantasymusic.vo.CurrentPlayerInfo
import com.frankhon.fantasymusic.vo.SimpleSong
import com.hon.mylogger.MyLogger
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_panel.*
import kotlinx.android.synthetic.main.layout_song_control.*

class MainActivity : AppCompatActivity(), PlayerLifecycleObserver, PlayerConfigurationObserver {

    override fun onCreate(savedInstanceState: Bundle?) {
        MyLogger.d("onCreate: ")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        connectAudioPlayer()
    }

    override fun onDestroy() {
        MyLogger.d("onDestroy: ")
        super.onDestroy()
        AudioPlayerManager.release()
    }

    /**
     * Android 12 behavior changes:
     * (Lifecycle) Root launcher activities are no longer finished on Back press
     */
    override fun onBackPressed() {
        MyLogger.d("onBackPressed: ")
        //返回之前先收起控制栏
        if (collapsePanel()) {
            return
        }
        super.onBackPressed()
    }

    override fun onPlayerConnected(playerInfo: CurrentPlayerInfo?) {
        playerInfo?.run {
            curSong?.let {
                updateSongPanel(it)
                updatePlayControlIcon(curPlayerState)
                updatePreviousNextButton(curPlayMode, curSongIndex, curPlaylist.size)
                ib_play_mode.playMode = PlayModeImageButton.State.valueOf(curPlayMode.name)
                updateSongDuration(it)
                // update progress
                tv_current_time.text = msToMMSS(curPlaybackPosition)
                sb_play_progress.progress = curPlaybackPosition.toInt()
            }
        }
    }

    override fun onPrepare(song: SimpleSong, playMode: PlayMode, curIndex: Int, totalSize: Int) {
        updateSongPanel(song)
        ib_pause_or_resume.setPlayState(ControlButtonState.PREPARING)
        updatePreviousNextButton(playMode, curIndex, totalSize)
        //更新播放列表中当前播放歌曲
        updatePlaylistPopup(ib_playlist, index = curIndex)
    }

    override fun onPlaying(song: SimpleSong) {
        updateSongDuration(song)
        updatePlayControlIcon(PlayerState.PLAYING)
    }

    override fun onAudioPause() {
        updatePlayControlIcon(PlayerState.PAUSED)
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
            ToastUtil.showToast(errorMsg)
        }
    }

    override fun onProgressUpdated(curPosition: Long, duration: Long) {
        tv_current_time.text = msToMMSS(curPosition)
        val isTracking = (sb_play_progress.tag as? Boolean) ?: false
        //未拖拽时更新进度条
        if (!isTracking) {
            sb_play_progress.progress = curPosition.toInt()
        }
    }

    override fun onPlayModeChanged(playMode: PlayMode, curIndex: Int, totalSize: Int) {
        updatePreviousNextButton(playMode, curIndex, totalSize)
    }

    override fun onPlaylistChanged(playMode: PlayMode, playlist: List<SimpleSong>, curIndex: Int) {
        updatePlaylistPopup(
            ib_playlist,
            newPlaylist = playlist.transferToSongItems(curIndex)
        )
        updatePreviousNextButton(playMode, curIndex, playlist.size)
    }

    private fun updatePlayControlIcon(playerState: PlayerState) {
        when (playerState) {
            PlayerState.PLAYING, PlayerState.RESUMED -> ib_pause_or_resume.setPlayState(
                ControlButtonState.PLAYING
            )
            PlayerState.PREPARING -> ib_pause_or_resume.setPlayState(ControlButtonState.PREPARING)
            else -> ib_pause_or_resume.setPlayState(ControlButtonState.PAUSED)
        }
    }

    private fun updateSongPanel(song: SimpleSong) {
        song.run {
            Glide.with(this@MainActivity)
                .load(songPic)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .apply(RequestOptions.circleCropTransform())
                .into(iv_song_bottom_pic)
            tv_bottom_song_name.text = name
            tv_bottom_artist_name.text = artist
            supl_main.setAllowDragging(true)
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
            tv_duration.text = msToMMSS(it.duration)
            sb_play_progress.run {
                max = it.duration.toInt()
            }
        }
    }

    private fun connectAudioPlayer() {
        AudioPlayerManager.connect {
            it.registerLifecycleObserver(this)
            it.registerProgressObserver(this)
        }
    }

    private fun initView() {
        ib_pause_or_resume.setOnControlButtonClickListener { curState ->
            when (curState) {
                ControlButtonState.PLAYING -> {
                    val currentPlayerInfo = AudioPlayerManager.getCurrentPlayerInfo()
                    currentPlayerInfo?.run {
                        if (curPlayerState == PlayerState.ERROR) {
                            AudioPlayerManager.play(curSong)
                        } else {
                            AudioPlayerManager.resume()
                        }
                    } ?: kotlin.run { AudioPlayerManager.resume() }
                }
                ControlButtonState.PAUSED -> AudioPlayerManager.pause()
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
            ToastUtil.showToast(toastText)
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
                showPlaylistPopup(it, curPlaylist.transferToSongItems(curSongIndex), {
                    AudioPlayerManager.removeSongFromPlayList(it)
                }) {
                    AudioPlayerManager.play(it)
                }
            }
        }
        setDefaultPanel()
    }

    private fun setDefaultPanel() {
        setDefaultImageToPanel()
        tv_current_time.text = msToMMSS(0)
        tv_duration.text = ""
        tv_bottom_song_name.text = getText(R.string.app_name)
        tv_bottom_artist_name.text = getText(R.string.welcome_text)
        ib_pause_or_resume.setPlayState(ControlButtonState.INITIAL)
        ib_previous_song.isEnabled = false
        ib_next_song.isEnabled = false
        sb_play_progress.progress = 0
        supl_main.setAllowDragging(false)
        collapsePanel()
    }

    private fun setDefaultImageToPanel() {
        Glide.with(this)
            .load(R.mipmap.ic_launcher)
            .apply(RequestOptions.circleCropTransform())
            .into(iv_song_bottom_pic)
    }

    private fun collapsePanel() = supl_main.collapse()
}
