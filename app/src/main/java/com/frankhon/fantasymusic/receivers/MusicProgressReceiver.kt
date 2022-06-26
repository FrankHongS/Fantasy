package com.frankhon.fantasymusic.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.frankhon.fantasymusic.utils.KEY_DURATION
import com.frankhon.fantasymusic.utils.KEY_SONG_PROGRESS
import com.frankhon.fantasymusic.vo.SongProgressEvent
import org.greenrobot.eventbus.EventBus

/**
 * Created by Frank_Hon on 11/12/2020.
 * E-mail: v-shhong@microsoft.com
 *
 * 跨进程时，使用EventBus通信失效
 */
class MusicProgressReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        intent.let {
            val progress = it.getIntExtra(KEY_SONG_PROGRESS, 0)
            val duration = it.getIntExtra(KEY_DURATION, 0)
            EventBus.getDefault().post(
                SongProgressEvent(progress, duration)
            )
        }
    }
}