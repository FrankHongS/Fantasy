package com.frankhon.fantasymusic.ui.view.panel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.media.*
import com.frankhon.fantasymusic.media.observer.PlayerConfigurationObserver
import com.frankhon.fantasymusic.media.observer.PlayerLifecycleObserver
import com.frankhon.fantasymusic.ui.activities.SongDetailActivity
import com.frankhon.fantasymusic.ui.view.AnimatedAudioCircleImageView
import com.frankhon.fantasymusic.ui.view.AnimatedAudioToggleButton
import com.frankhon.fantasymusic.utils.*
import com.frankhon.fantasymusic.utils.popup.dismissPlaylistPopup
import com.frankhon.fantasymusic.utils.popup.showPlaylistPopup
import com.frankhon.fantasymusic.utils.popup.updatePlaylistPopup
import com.frankhon.fantasymusic.vo.CurrentPlayerInfo
import com.frankhon.fantasymusic.vo.SimpleSong
import com.hon.mylogger.MyLogger
import kotlinx.android.synthetic.main.layout_simple_song_control.view.*

/**
 * 通用底部控制栏
 *
 * Created by Frank Hon on 2022/10/28 11:07 下午.
 * E-mail: frank_hon@foxmail.com
 */
class SimpleBottomControlPanel @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), PlayerLifecycleObserver,
    PlayerConfigurationObserver {

    private val albumImage: AnimatedAudioCircleImageView
    private val songName: TextView
    private val artistName: TextView
    private val toggleButton: AnimatedAudioToggleButton

    init {
        View.inflate(context, R.layout.layout_simple_panel, this)

        albumImage = findViewById(R.id.iv_song_bottom_pic)
        songName = findViewById(R.id.tv_bottom_song_name)
        artistName = findViewById(R.id.tv_bottom_artist_name)
        toggleButton = findViewById(R.id.ib_player_toggle)

        setBackgroundColor(context.color(R.color.navigationBarColor))
        //阴影只在下方生效，此处设置无意义，如果需要实现阴影可借助CardView
        ViewCompat.setElevation(this, dimen(R.dimen.dp_8))
        setDefaultPanel()

        toggleButton.bindClickListener()

        ib_next_song.setOnClickListener {
            AudioPlayerManager.next()
        }
        ib_panel_playlist.setOnClickListener {
            val currentPlayerInfo = AudioPlayerManager.getCurrentPlayerInfo()
            currentPlayerInfo?.run {
                it.showPlaylistPopup(curPlaylist.transferToSongItems(curSongIndex),
                    hOffset = (-230).dp, {
                        AudioPlayerManager.removeSongFromPlayList(it)
                    }) {
                    AudioPlayerManager.play(it)
                }
            }
        }
        setOnClickListener {
            AudioPlayerManager.getCurrentPlayerInfo()?.takeIf { !it.curPlayerState.isStopped() }
                ?.run {
                    context.navigateWithTransitions<SongDetailActivity>()
                }
        }
    }

    fun connectAudioPlayer() {
        MyLogger.d("connectAudioPlayer: ")
        // 注册时使用connect，以免与播放器的连接断开
        AudioPlayerManager.connect(object : AudioPlayerManager.OnServiceConnectedListener {
            override fun onServiceConnected(manager: AudioPlayerManager) {
                manager.registerLifecycleObserver(this@SimpleBottomControlPanel)
                manager.registerConfigurationObserver(this@SimpleBottomControlPanel)
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
        ib_panel_playlist.dismissPlaylistPopup()
    }

    //region Audio lifecycle

    override fun onPlayerConnected(playerInfo: CurrentPlayerInfo?) {
        playerInfo?.run {
            if (curPlayerState.isStopped()) {
                setDefaultPanel()
                return
            }
            curSong?.let {
                updateSongPanel(it)
                toggleButton.updatePlayControlIcon(curPlayerState, false)
                updateNextButton(curPlayMode, curSongIndex, curPlaylist.size)
                // update album image
                if (curPlayerState.isPlaying()) {
                    songName.isSelected = true
                    albumImage.startRotateAnimator()
                } else {
                    songName.isSelected = false
                    albumImage.cancelRotateAnimator()
                }
                albumImage.updateProgress(curPlaybackPosition.toInt(), it.duration.toInt())
            }
        }
    }

    override fun onPrepare(song: SimpleSong, playMode: PlayMode, curIndex: Int, totalSize: Int) {
        albumImage.cancelRotateAnimator()
        updateSongPanel(song)
        toggleButton.updatePlayControlIcon(PlayerState.PREPARING)
        updateNextButton(playMode, curIndex, totalSize)
        //更新播放列表中当前播放歌曲
        ib_panel_playlist.updatePlaylistPopup(index = curIndex)
    }

    override fun onPlaying(song: SimpleSong) {
        toggleButton.updatePlayControlIcon(PlayerState.PLAYING)
        albumImage.startRotateAnimator()
        songName.isSelected = true
    }

    override fun onAudioResume(song: SimpleSong) {
        toggleButton.updatePlayControlIcon(PlayerState.RESUMED)
        albumImage.resumeRotateAnimator()
        songName.isSelected = true
    }

    override fun onAudioPause() {
        toggleButton.updatePlayControlIcon(PlayerState.PAUSED)
        albumImage.pauseRotateAnimator()
        songName.isSelected = false
    }

    override fun onAudioStop() {
        setDefaultPanel()
    }

    override fun onFinished() {
        toggleButton.updatePlayControlIcon(PlayerState.FINISHED)
    }

    override fun onError(errorMsg: String) {
        toggleButton.updatePlayControlIcon(PlayerState.PAUSED)
        if (errorMsg.isNotEmpty()) {
            showToast(errorMsg)
        }
    }

    // endregion

    override fun onProgressUpdated(curPosition: Long, duration: Long) {
        albumImage.updateProgress(curPosition.toInt(), duration.toInt())
    }

    override fun onPlaylistChanged(playMode: PlayMode, playlist: List<SimpleSong>, curIndex: Int) {
        ib_panel_playlist.updatePlaylistPopup(
            newPlaylist = playlist.transferToSongItems(curIndex)
        )
        updateNextButton(playMode, curIndex, playlist.size)
    }

    private fun setDefaultPanel() {
        songName.run {
            text = context.getText(R.string.app_name)
            isSelected = false
        }
        artistName.text = context.getText(R.string.welcome_text)
        albumImage.reset(R.drawable.default_placeholder)
        ib_next_song.isEnabled = false
        toggleButton.setPlayState(AnimatedAudioToggleButton.ControlButtonState.INITIAL)
    }

    private fun updateSongPanel(song: SimpleSong) {
        song.run {
            Glide.with(context)
                .load(picUrl)
                .placeholder(R.drawable.default_placeholder)
                .error(R.drawable.default_placeholder)
                .into(albumImage)
            songName.text = name
            artistName.text = artist
        }
    }

    private fun updateNextButton(curPlayMode: PlayMode, curIndex: Int, totalSize: Int) {
        if (totalSize == 1) {
            ib_next_song.isEnabled = false
            return
        }
        if (curPlayMode == PlayMode.LOOP_SINGLE) {
            ib_next_song.isEnabled = curIndex != totalSize - 1
        } else {
            ib_next_song.isEnabled = true
        }
    }
}