package com.frankhon.fantasymusic.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.media.PlayerState
import com.frankhon.fantasymusic.utils.KEY_CUR_SONG
import com.frankhon.fantasymusic.utils.KEY_PLAYER_ERROR_MESSAGE
import com.frankhon.fantasymusic.utils.KEY_PLAYER_STATE
import com.frankhon.fantasymusic.vo.PlayingSongEvent
import com.frankhon.fantasymusic.vo.SimpleSong
import org.greenrobot.eventbus.EventBus

/**
 * Created by Frank_Hon on 11/12/2020.
 * E-mail: v-shhong@microsoft.com
 *
 * 跨进程时，使用EventBus通信失效
 */
class MusicInfoReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        intent.let {
            val errorMsg = it.getStringExtra(KEY_PLAYER_ERROR_MESSAGE).orEmpty()
            val song = it.getParcelableExtra<SimpleSong>(KEY_CUR_SONG)
            val state = it.getSerializableExtra(KEY_PLAYER_STATE) as? PlayerState
            if (state != null) {
                AudioPlayerManager.publishPlayerState(song, state, errorMsg)
            }
        }
    }
}