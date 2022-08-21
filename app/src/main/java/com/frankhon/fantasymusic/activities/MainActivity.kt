package com.frankhon.fantasymusic.activities

import android.app.NotificationManager
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.media.PlayerState
import com.frankhon.fantasymusic.utils.PLAYER_CHANNEL_ID
import com.frankhon.fantasymusic.utils.ToastUtil
import com.frankhon.fantasymusic.utils.Util
import com.frankhon.fantasymusic.utils.msToMMSS
import com.frankhon.fantasymusic.view.AnimatedAudioControlButton.PlayState
import com.frankhon.fantasymusic.view.PlayModeImageButton
import com.frankhon.fantasymusic.vo.PlayingSongEvent
import com.frankhon.fantasymusic.vo.SongProgressEvent
import kotlinx.android.synthetic.main.layout_panel.*
import kotlinx.android.synthetic.main.layout_song_control.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

private const val SONG_EVENT_KEY = "playSongEvent"

class MainActivity : AppCompatActivity() {

    private var isPlaying = false
    private var curEvent: PlayingSongEvent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        EventBus.getDefault().register(this)
        AudioPlayerManager.connect()
        createNotificationChannel()

        initView()
        // todo unregister
//        registerReceiver(MusicInfoReceiver(), IntentFilter(MUSIC_INFO_ACTION))
    }

    private fun initView() {
        ib_pause_or_resume.setOnControlButtonClickListener { curState ->
            when (curState) {
                PlayState.PLAYING -> AudioPlayerManager.resume()
                PlayState.PAUSED -> AudioPlayerManager.pause()
                else -> {}
            }
        }
        ib_next_song.setOnClickListener {
            AudioPlayerManager.next()
        }
        ib_previous_song.setOnClickListener {
            AudioPlayerManager.previous()
        }
        setDefaultImageToPanel()
        ib_play_mode.setObserver {
            ToastUtil.showToast(
                when (it) {
                    PlayModeImageButton.PlayMode.SHUFFLE -> "Shuffle"
                    PlayModeImageButton.PlayMode.LOOP_SINGLE -> "Single loop"
                    PlayModeImageButton.PlayMode.LOOP_LIST -> "List loop"
                }
            )
        }
        tv_current_time.text = msToMMSS(0)
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
    }

    @Subscribe
    fun updatePanel(event: PlayingSongEvent) {
        update(event)
    }

    @Subscribe
    fun onProgressEventReceived(event: SongProgressEvent) {
        tv_current_time.text = msToMMSS(event.progress.toLong())
        val isTracking = (sb_play_progress.tag as? Boolean) ?: false
        //未拖拽时更新进度条
        if (!isTracking) {
            sb_play_progress.progress = event.progress
        }
    }

    private fun setDefaultImageToPanel() {
        Glide.with(this)
            .load(R.mipmap.ic_launcher)
            .apply(RequestOptions.circleCropTransform())
            .into(iv_song_bottom_pic)
    }

    private fun update(event: PlayingSongEvent) {
        curEvent = event
        updatePlayControlIcon(event.playerState)
        if (event.playerState == PlayerState.PLAYING) {
            val song = event.song
            song?.run {
                songPic?.takeIf { it.isNotEmpty() }?.let {
                    Glide.with(this@MainActivity)
                        .load(it)
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .apply(RequestOptions.circleCropTransform())
                        .into(iv_song_bottom_pic)
                } ?: kotlin.run {
                    setDefaultImageToPanel()
                }
                tv_bottom_song_name.text = name
                tv_bottom_artist_name.text = artist
                tv_duration.text = msToMMSS(duration)
                sb_play_progress.run {
                    max = duration.toInt()
                }
            }
        }
    }

    private fun updatePlayControlIcon(isPlaying: Boolean) {
        if (isPlaying) {
            ib_pause_or_resume.setPlayState(PlayState.PLAYING)
        } else {
            ib_pause_or_resume.setPlayState(PlayState.PAUSED)
        }
    }

    private fun updatePlayControlIcon(playerState: PlayerState) {
        when (playerState) {
            PlayerState.PLAYING -> ib_pause_or_resume.setPlayState(PlayState.PLAYING)
            PlayerState.PAUSED, PlayerState.FINISHED -> ib_pause_or_resume.setPlayState(PlayState.PAUSED)
            PlayerState.PREPARING -> ib_pause_or_resume.setPlayState(PlayState.PREPARING)
            else -> {}
        }
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Util.createNotificationChannel(
                PLAYER_CHANNEL_ID,
                PLAYER_CHANNEL_ID,
                NotificationManager.IMPORTANCE_LOW
            )
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        curEvent = savedInstanceState.getParcelable(SONG_EVENT_KEY)
        curEvent?.let { update(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        curEvent?.let { outState.putParcelable(SONG_EVENT_KEY, it) }
    }


    override fun onDestroy() {
        super.onDestroy()
        //todo bug 横竖屏切换和系统回收重建不应release，否则会crash
//        MediaPlayerManager.getInstance().release()
    }
}
