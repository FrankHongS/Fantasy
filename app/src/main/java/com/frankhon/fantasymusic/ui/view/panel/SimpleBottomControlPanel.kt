package com.frankhon.fantasymusic.ui.view.panel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.media.PlayMode
import com.frankhon.fantasymusic.media.PlayerState
import com.frankhon.fantasymusic.media.isPlaying
import com.frankhon.fantasymusic.media.observer.PlayerConfigurationObserver
import com.frankhon.fantasymusic.media.observer.PlayerLifecycleObserver
import com.frankhon.fantasymusic.ui.view.AnimatedAudioCircleImageView
import com.frankhon.fantasymusic.ui.view.AnimatedAudioToggleButton
import com.frankhon.fantasymusic.utils.color
import com.frankhon.fantasymusic.utils.dp
import com.frankhon.fantasymusic.utils.popup.dismissPlaylistPopup
import com.frankhon.fantasymusic.utils.popup.showPlaylistPopup
import com.frankhon.fantasymusic.utils.popup.updatePlaylistPopup
import com.frankhon.fantasymusic.utils.showToast
import com.frankhon.fantasymusic.utils.transferToSongItems
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
        ViewCompat.setElevation(this, 8.dp.toFloat())
        setDefaultPanel()

        toggleButton.setOnControlButtonClickListener { curState ->
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

    }

    override fun onAttachedToWindow() {
        MyLogger.d("onAttachedToWindow: ")
        super.onAttachedToWindow()
        // 注册时使用connect，以免与播放器的连接断开
        AudioPlayerManager.connect {
            it.registerLifecycleObserver(this)
            it.registerConfigurationObserver(this)
        }
    }

    override fun onDetachedFromWindow() {
        MyLogger.d("onDetachedFromWindow: ")
        super.onDetachedFromWindow()
        ib_panel_playlist.dismissPlaylistPopup()
        AudioPlayerManager.let {
            it.unregisterLifecycleObserver(this)
            it.unregisterConfigurationObserver(this)
        }
    }

    //region Audio lifecycle

    override fun onPlayerConnected(playerInfo: CurrentPlayerInfo?) {
        playerInfo?.run {
            curSong?.let {
                updateSongPanel(it)
                updatePlayControlIcon(curPlayerState, false)
                updateNextButton(curPlayMode, curSongIndex, curPlaylist.size)
                // update album image
                if (curPlayerState.isPlaying()) {
                    songName.isSelected = true
                    albumImage.startRotateAnimator()
                } else {
                    songName.isSelected = false
                    albumImage.cancelRotateAnimator()
                }
                albumImage.startUpdateProgress(curPlaybackPosition.toInt(), it.duration.toInt())
            }
        }
    }

    override fun onPrepare(song: SimpleSong, playMode: PlayMode, curIndex: Int, totalSize: Int) {
        albumImage.pauseRotateAnimator()
        updateSongPanel(song)
        updatePlayControlIcon(PlayerState.PREPARING)
        updateNextButton(playMode, curIndex, totalSize)
        //更新播放列表中当前播放歌曲
        ib_panel_playlist.updatePlaylistPopup(index = curIndex)
    }

    override fun onPlaying(song: SimpleSong) {
        updatePlayControlIcon(PlayerState.PLAYING)
        albumImage.startRotateAnimator()
        songName.isSelected = true
    }

    override fun onAudioResume(song: SimpleSong) {
        updatePlayControlIcon(PlayerState.RESUMED)
        albumImage.resumeRotateAnimator()
        songName.isSelected = true
    }

    override fun onAudioPause() {
        updatePlayControlIcon(PlayerState.PAUSED)
        albumImage.pauseRotateAnimator()
        songName.isSelected = false
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

    // endregion

    override fun onProgressUpdated(curPosition: Long, duration: Long) {
        albumImage.startUpdateProgress(curPosition.toInt(), duration.toInt())
    }

    override fun onPlaylistChanged(playMode: PlayMode, playlist: List<SimpleSong>, curIndex: Int) {
        ib_panel_playlist.updatePlaylistPopup(
            newPlaylist = playlist.transferToSongItems(curIndex)
        )
        updateNextButton(playMode, curIndex, playlist.size)
    }

    // tricky 通过通知栏切歌，然后暂停，专辑图片一直旋转；应该是属性动画在后台无法正常暂停，以下为临时处理方案
    fun doOnResume() {
        val currentPlayerInfo = AudioPlayerManager.getCurrentPlayerInfo()
        currentPlayerInfo?.run {
            if (curPlayerState == PlayerState.PAUSED) {
                albumImage.pauseRotateAnimator()
            }
        }
    }

    private fun setDefaultPanel() {
        songName.run {
            text = context.getText(R.string.app_name)
            isSelected = false
        }
        artistName.text = context.getText(R.string.welcome_text)
        albumImage.run {
            setImageResource(R.mipmap.ic_launcher)
            cancelRotateAnimator()
            resetProgress()
        }
        ib_next_song.isEnabled = false
        toggleButton.setPlayState(AnimatedAudioToggleButton.ControlButtonState.INITIAL)
    }

    private fun updateSongPanel(song: SimpleSong) {
        song.run {
            Glide.with(context)
                .load(picUrl)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(albumImage)
            songName.text = name
            artistName.text = artist
        }
    }

    private fun updatePlayControlIcon(playerState: PlayerState, shouldAnimate: Boolean = true) {
        when (playerState) {
            PlayerState.PLAYING, PlayerState.RESUMED -> toggleButton.setPlayState(
                AnimatedAudioToggleButton.ControlButtonState.PLAYING, shouldAnimate
            )
            PlayerState.PREPARING -> toggleButton.setPlayState(
                AnimatedAudioToggleButton.ControlButtonState.PREPARING,
                shouldAnimate
            )
            else -> toggleButton.setPlayState(
                AnimatedAudioToggleButton.ControlButtonState.PAUSED,
                shouldAnimate
            )
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