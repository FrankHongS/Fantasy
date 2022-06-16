package com.frankhon.fantasymusic.activities

import android.app.NotificationManager
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.receivers.MusicInfoReceiver
import com.frankhon.fantasymusic.utils.*
import com.frankhon.fantasymusic.view.PlayModeImageButton
import com.frankhon.fantasymusic.vo.PlaySongEvent
import kotlinx.android.synthetic.main.layout_panel.*
import kotlinx.android.synthetic.main.layout_song_control.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

private const val SONG_EVENT_KEY = "playSongEvent"

class MainActivity : AppCompatActivity() {

    private var isPlaying = false
    private var curEvent: PlaySongEvent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        EventBus.getDefault().register(this)
        AudioPlayerManager.getInstance().init()
        createNotificationChannel()

        initView()
        // todo unregister
        registerReceiver(MusicInfoReceiver(), IntentFilter(MUSIC_INFO_ACTION))
    }

    private fun initView() {
        ib_pause_or_resume.setOnClickListener {
            if (isPlaying) {
                AudioPlayerManager.getInstance().pause()
            } else {
                AudioPlayerManager.getInstance().resume()
            }
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
    }

    @Subscribe
    fun updatePanel(event: PlaySongEvent) {
        update(event)
    }

    private fun setDefaultImageToPanel() {
        Glide.with(this)
            .load(R.mipmap.ic_launcher)
            .apply(RequestOptions.circleCropTransform())
            .into(iv_song_bottom_pic)
    }

    private fun update(event: PlaySongEvent) {
        curEvent = event
        if (event.isResumed) {
            isPlaying = true
            updatePlayControlIcon(isPlaying)
            return
        } else {
            isPlaying = event.isPlaying
            updatePlayControlIcon(isPlaying)
        }
        if (isPlaying) {
            val song = event.song
            song?.run {
                songPic?.takeIf { it.isNotEmpty() }?.let {
                    Glide.with(this@MainActivity)
                        .load(it)
                        .apply(RequestOptions.circleCropTransform())
                        .into(iv_song_bottom_pic)
                } ?: kotlin.run {
                    setDefaultImageToPanel()
                }
                tv_bottom_song_name.text = name
                tv_bottom_artist_name.text = artist
                tv_current_time.text = msToMMSS(0)
                tv_duration.text = msToMMSS(duration)
            }
        }
    }

    private fun updatePlayControlIcon(isPlaying: Boolean) {
        if (isPlaying) {
            ib_pause_or_resume.setImageResource(R.drawable.ic_pause_song)
        } else {
            ib_pause_or_resume.setImageResource(R.drawable.ic_play_song)
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
        curEvent = savedInstanceState.getParcelable<PlaySongEvent?>(SONG_EVENT_KEY)
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
