package com.frankhon.fantasymusic.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.frankhon.fantasymusic.utils.Constants
import com.frankhon.fantasymusic.vo.PlaySongEvent
import org.greenrobot.eventbus.EventBus

/**
 * Created by Frank_Hon on 11/12/2020.
 * E-mail: v-shhong@microsoft.com
 *
 * 跨进程时，使用EventBus通信失效
 */
class MusicInfoReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val state = intent!!.getIntExtra(Constants.KEY_PLAYER_STATE, -1)
        when (state) {
            2 -> EventBus.getDefault().post(PlaySongEvent(isResumed = true))
            3 -> EventBus.getDefault().post(PlaySongEvent())
            4 -> EventBus.getDefault().post(PlaySongEvent())
            5 -> EventBus.getDefault().post(PlaySongEvent())
        }
    }
}