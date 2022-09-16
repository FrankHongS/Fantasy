package com.frankhon.fantasymusic.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.media.PlayerConfiguration
import com.frankhon.fantasymusic.utils.KEY_PLAYER_CONFIG
import com.frankhon.fantasymusic.utils.KEY_PLAY_MODE

/**
 * Created by Frank Hon on 2022/9/13 12:30 上午.
 * E-mail: frank_hon@foxmail.com
 */
class MusicPlayerConfigurationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        intent.let {
            val playerConfigStr = it.getStringExtra(KEY_PLAYER_CONFIG)
            if (!playerConfigStr.isNullOrEmpty()) {
                when (PlayerConfiguration.valueOf(playerConfigStr)) {
                    PlayerConfiguration.PLAY_MODE -> {
                        val playMode = it.getStringExtra(KEY_PLAY_MODE)
                        if (!playMode.isNullOrEmpty()) {
                            AudioPlayerManager.publishPlayMode(playMode)
                        }
                    }
                    PlayerConfiguration.PLAYLIST ->
                        AudioPlayerManager.publishPlaylistChanged()
                }
            }
        }
    }

}