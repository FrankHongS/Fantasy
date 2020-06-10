package com.frankhon.fantasymusic.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.media.MediaPlayerManager
import com.frankhon.fantasymusic.vo.PlaySongEvent
import kotlinx.android.synthetic.main.layout_panel.*
import kotlinx.android.synthetic.main.layout_song_control.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

private const val SONG_EVENT_KEY = "playSongEvent"

class MainActivity : AppCompatActivity() {

    private var isPlaying = false
    private var event: PlaySongEvent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)

        ib_pause_or_resume.setOnClickListener {
            if (isPlaying) {
                MediaPlayerManager.getInstance().pause()
            } else {
                MediaPlayerManager.getInstance().resume()
            }
            isPlaying = !isPlaying
            updatePlayControlIcon(isPlaying)
        }
    }

    @Subscribe
    fun updatePanel(event: PlaySongEvent) {
        update(event)
    }

    private fun update(event: PlaySongEvent) {
        this.event = event

        updatePlayControlIcon(event.isPlaying)
        isPlaying = event.isPlaying
        if (isPlaying) {
            if (event.picUrl != null) {
                Glide.with(this)
                    .load(event.picUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(iv_song_bottom_pic)
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

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        event = savedInstanceState?.getParcelable<PlaySongEvent?>(SONG_EVENT_KEY)
        event?.let { update(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        event?.let { outState.putParcelable(SONG_EVENT_KEY, it) }
    }


    override fun onDestroy() {
        super.onDestroy()
        //todo bug 横竖屏切换和系统回收重建不应release，否则会crash
//        MediaPlayerManager.getInstance().release()
    }
}
