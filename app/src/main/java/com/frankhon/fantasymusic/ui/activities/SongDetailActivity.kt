package com.frankhon.fantasymusic.ui.activities

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.media.*
import com.frankhon.fantasymusic.media.observer.PlayerConfigurationObserver
import com.frankhon.fantasymusic.media.observer.PlayerLifecycleObserver
import com.frankhon.fantasymusic.ui.view.PlayModeImageButton
import com.frankhon.fantasymusic.utils.*
import com.frankhon.fantasymusic.utils.popup.showPlaylistPopup
import com.frankhon.fantasymusic.utils.popup.updatePlaylistPopup
import com.frankhon.fantasymusic.vo.CurrentPlayerInfo
import com.frankhon.fantasymusic.vo.SimpleSong
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_song_detail.*
import kotlinx.coroutines.launch

/**
 * Created by Frank Hon on 2022/11/23 11:56 上午.
 * E-mail: frank_hon@foxmail.com
 */
class SongDetailActivity : AppCompatActivity(), PlayerLifecycleObserver,
    PlayerConfigurationObserver {

    private var shouldShowLyrics = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_detail)
        toImmersiveMode()
        initView()
        connectAudioPlayer()
    }

    override fun onResume() {
        super.onResume()
        // workaround 该activity使用了transition，在切后台的时候会将所有View设置为可见
        updateLyricsVisibility(shouldShowLyrics)
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectAudioPlayer()
    }

    override fun onPlayerConnected(playerInfo: CurrentPlayerInfo?) {
        playerInfo?.run {
            curSong?.let {
                syncSongInfo(it)
                // update progress
                updateSongDuration(it)
                tv_current_time_detail.text = msToMMSS(curPlaybackPosition)
                sb_play_progress_detail.progress = curPlaybackPosition.toInt()
                loadLyrics(it) {
                    lv_lyrics_detail.updateCurLyricTime(curPlaybackPosition, false)
                }
                btn_toggle_detail.updatePlayControlIcon(curPlayerState, false)
                updatePreviousNextButton(curPlayMode, curSongIndex, curPlaylist.size)
                updateTitle(curPlayerState.isPlaying())
                ib_play_mode_detail.playMode = PlayModeImageButton.State.valueOf(curPlayMode.name)
            }
        }
    }

    override fun onPrepare(song: SimpleSong, playMode: PlayMode, curIndex: Int, totalSize: Int) {
        song.let {
            syncSongInfo(it)
            loadLyrics(it)
        }
        btn_toggle_detail.updatePlayControlIcon(PlayerState.PREPARING)
        updatePreviousNextButton(playMode, curIndex, totalSize)
        ib_playlist_detail.updatePlaylistPopup(index = curIndex)
    }

    override fun onPlaying(song: SimpleSong) {
        updateSongDuration(song)
        btn_toggle_detail.updatePlayControlIcon(PlayerState.PLAYING)
        updateTitle(true)
    }

    override fun onAudioResume(song: SimpleSong) {
        btn_toggle_detail.updatePlayControlIcon(PlayerState.RESUMED)
        updateTitle(true)
    }

    override fun onAudioPause() {
        btn_toggle_detail.updatePlayControlIcon(PlayerState.PAUSED)
        updateTitle(false)
    }

    override fun onAudioStop() {
        finish()
    }

    override fun onError(errorMsg: String) {
        btn_toggle_detail.updatePlayControlIcon(PlayerState.PAUSED)
        showToast(errorMsg)
    }

    override fun onProgressUpdated(curPosition: Long, duration: Long) {
        tv_current_time_detail.text = msToMMSS(curPosition)
        sb_play_progress_detail.run {
            //未拖拽时更新进度条
            if (!isTracking) {
                progress = curPosition.toInt()
            }
        }
        lv_lyrics_detail.updateCurLyricTime(curPosition)
    }

    override fun onPlayModeChanged(playMode: PlayMode, curIndex: Int, totalSize: Int) {
        updatePreviousNextButton(playMode, curIndex, totalSize)
    }

    override fun onPlaylistChanged(playMode: PlayMode, playlist: List<SimpleSong>, curIndex: Int) {
        ib_playlist_detail.updatePlaylistPopup(
            newPlaylist = playlist.transferToSongItems(curIndex)
        )
        updatePreviousNextButton(playMode, curIndex, playlist.size)
    }

    private fun syncSongInfo(song: SimpleSong) {
        song.run {
            tv_song_name_detail.text = name
            tv_artist_detail.text = artist
            Glide.with(this@SongDetailActivity)
                .load(picUrl)
                .placeholder(ColorDrawable(color(R.color.colorPrimary)))
                .transition(DrawableTransitionOptions.withCrossFade(800))
                .apply(RequestOptions.bitmapTransform(BlurTransformation(45, 3)))
                .into(iv_background)
            Glide.with(this@SongDetailActivity)
                .load(picUrl)
                .placeholder(R.drawable.default_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .into(iv_album_detail)
        }
    }

    private fun loadLyrics(song: SimpleSong, action: (() -> Unit)? = null) {
        lifecycleScope.launch {
            lv_lyrics_detail.reset()
            LyricsManager.loadLyrics(song)?.let { lyrics ->
                lv_lyrics_detail.setLyrics(lyrics, action)
            }
        }
    }

    private fun connectAudioPlayer() {
        AudioPlayerManager.connect(object : AudioPlayerManager.OnServiceConnectedListener {
            override fun onServiceConnected(manager: AudioPlayerManager) {
                manager.registerLifecycleObserver(this@SongDetailActivity)
                manager.registerConfigurationObserver(this@SongDetailActivity)
            }
        })
    }

    private fun disconnectAudioPlayer() {
        AudioPlayerManager.let {
            it.unregisterLifecycleObserver(this)
            it.unregisterConfigurationObserver(this)
        }
    }

    private fun initView() {
        ib_back_detail.setOnClickListener { onBackPressed() }
        btn_toggle_detail.bindClickListener()
        ib_previous_detail.run {
            setOnClickListener {
                AudioPlayerManager.previous()
            }
            isEnabled = false
        }
        ib_next_detail.run {
            setOnClickListener {
                AudioPlayerManager.next()
            }
            isEnabled = false
        }
        ib_play_mode_detail.bindClickListener()
        ib_playlist_detail.setOnClickListener {
            val currentPlayerInfo = AudioPlayerManager.getCurrentPlayerInfo()
            currentPlayerInfo?.run {
                it.showPlaylistPopup(curPlaylist.transferToSongItems(curSongIndex),
                    hOffset = (-200).dp, {
                        AudioPlayerManager.removeSongFromPlayList(it)
                    }) {
                    AudioPlayerManager.play(it)
                }
            }
        }

        Glide.with(this@SongDetailActivity)
            .load(ColorDrawable(color(R.color.colorPrimary)))
            .apply(RequestOptions.bitmapTransform(BlurTransformation(45, 3)))
            .into(iv_background)
        tv_current_time_detail.text = msToMMSS(0)
        tv_song_duration_detail.text = msToMMSS(0)
        sb_play_progress_detail.bindChangeListener()
        lv_lyrics_detail.setOnLyricsClickListener {
            updateLyricsVisibility(it.isInvisible)
        }
        lyrics_mask.setOnClickListener {
            updateLyricsVisibility(it.isVisible)
        }
    }

    private fun updateLyricsVisibility(isLyricsVisible: Boolean) {
        shouldShowLyrics = isLyricsVisible
        // 设置GONE获取不到width，此处需要设置为INVISIBLE
        lv_lyrics_detail.isInvisible = !isLyricsVisible
        iv_album_detail.isVisible = !isLyricsVisible
        lyrics_mask.isVisible = !isLyricsVisible
    }

    private fun updatePreviousNextButton(curPlayMode: PlayMode, curIndex: Int, totalSize: Int) {
        if (totalSize == 0 || totalSize == 1) {
            ib_previous_detail.isEnabled = false
            ib_next_detail.isEnabled = false
            return
        }
        if (curPlayMode == PlayMode.LOOP_SINGLE) {
            ib_previous_detail.isEnabled = curIndex != 0
            ib_next_detail.isEnabled = curIndex != totalSize - 1
        } else {
            ib_previous_detail.isEnabled = true
            ib_next_detail.isEnabled = true
        }
    }

    private fun updateSongDuration(song: SimpleSong) {
        song.let {
            tv_song_duration_detail.text = msToMMSS(it.duration)
            sb_play_progress_detail.run {
                max = it.duration.toInt()
            }
        }
    }

    private fun updateTitle(isPlaying: Boolean) {
        tv_song_name_detail.isSelected = isPlaying
        tv_artist_detail.isSelected = isPlaying
    }

}