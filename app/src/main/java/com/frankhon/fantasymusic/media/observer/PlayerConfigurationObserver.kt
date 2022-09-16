package com.frankhon.fantasymusic.media.observer

import com.frankhon.fantasymusic.media.PlayMode
import com.frankhon.fantasymusic.vo.SimpleSong

/**
 * Created by Frank Hon on 2022/8/29 2:34 下午.
 * E-mail: frank_hon@foxmail.com
 */
interface PlayerConfigurationObserver {

    /**
     * 播放进度
     */
    fun onProgressUpdated(curPosition: Long, duration: Long)

    /**
     * 播放顺序
     */
    fun onPlayModeChanged(playMode: PlayMode, curIndex: Int, totalSize: Int) {}

    /**
     * 播放列表
     */
    fun onPlaylistChanged(playMode: PlayMode, playlist: List<SimpleSong>, curIndex: Int) {}

}