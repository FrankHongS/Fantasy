package com.frankhon.fantasymusic.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.frankhon.fantasymusic.media.PlayerState
import com.frankhon.fantasymusic.utils.*
import com.frankhon.fantasymusic.vo.PlaySongEvent
import com.frankhon.fantasymusic.vo.SimpleSong
import org.greenrobot.eventbus.EventBus

/**
 * Created by Frank_Hon on 11/12/2020.
 * E-mail: v-shhong@microsoft.com
 *
 * 跨进程时，使用EventBus通信失效
 */
class MusicInfoReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val stateName = it.getStringExtra(KEY_PLAYER_STATE)
            val picUrl = it.getStringExtra(KEY_PIC_URL)
            val songName = it.getStringExtra(KEY_SONG_NAME)
            val artistName = it.getStringExtra(KEY_ARTIST_NAME)
            val duration = it.getLongExtra(KEY_DURATION, 0L)
            val song = it.getParcelableExtra<SimpleSong>(KEY_CUR_SONG)
            val state = if (stateName == null) {
                null
            } else {
                PlayerState.valueOf(stateName)
            }
            var event: PlaySongEvent? = null
            when (state) {
                PlayerState.PLAYING -> event = PlaySongEvent(
                    song = song,
                    isPlaying = true
                )
                PlayerState.RESUMED -> event = PlaySongEvent(
                    song = song,
                    isResumed = true
                )
                PlayerState.PAUSED -> event = PlaySongEvent(
                    song = song
                )
                PlayerState.TRANSIENT_PAUSED -> event = PlaySongEvent(
                    song = song
                )
                PlayerState.COMPLETED -> event = PlaySongEvent(
                    song = song
                )
                else -> {}
            }
            if (event != null) {
                EventBus.getDefault().post(event)
            }
        }
    }
}