package com.frankhon.fantasymusic.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
        EventBus.getDefault().post(PlaySongEvent())
    }
}