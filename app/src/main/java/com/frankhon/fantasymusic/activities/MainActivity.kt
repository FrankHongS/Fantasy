package com.frankhon.fantasymusic.activities

import android.app.NotificationManager
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.receivers.MusicInfoReceiver
import com.frankhon.fantasymusic.utils.Constants
import com.frankhon.fantasymusic.utils.Util
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
        registerReceiver(MusicInfoReceiver(), IntentFilter(Constants.MUSIC_INFO_ACTION))
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
            if (!TextUtils.isEmpty(event.picUrl)) {
                Glide.with(this)
                    .load(event.picUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(iv_song_bottom_pic)
            } else {
                setDefaultImageToPanel()
            }
            tv_bottom_song_name.text = event.songName
            tv_bottom_artist_name.text = event.artistName
        }
    }

    private fun updatePlayControlIcon(isPlaying: Boolean) {
        if (isPlaying) {
            ib_pause_or_resume.setImageResource(R.drawable.ic_pause_song)
        } else {
            ib_pause_or_resume.setImageResource(R.drawable.ic_play_song)
        }
    }

    private fun createNotificationChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Util.createNotificationChannel(
                Constants.PLAYER_CHANNEL_ID, Constants.PLAYER_CHANNEL_ID,
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
