package com.frankhon.fantasymusic.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.media.PlayMode
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
            when (it.getSerializableExtra(KEY_PLAYER_CONFIG) as? PlayerConfiguration) {
                PlayerConfiguration.PLAY_MODE -> {
                    val playMode = it.getSerializableExtra(KEY_PLAY_MODE) as? PlayMode
                    if (playMode != null) {
                        AudioPlayerManager.publishPlayMode(playMode)
                    }
                }
                PlayerConfiguration.PLAYLIST ->
                    AudioPlayerManager.publishPlaylistChanged()
                else -> {}
            }
        }
    }

}