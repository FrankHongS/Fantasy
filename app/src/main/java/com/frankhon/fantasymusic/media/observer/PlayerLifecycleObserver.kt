package com.frankhon.fantasymusic.media.observer

import com.frankhon.fantasymusic.media.PlayMode
import com.frankhon.fantasymusic.vo.CurrentPlayerInfo
import com.frankhon.fantasymusic.vo.SimpleSong

/**
 * Created by Frank Hon on 2022/6/14 10:43 下午.
 * E-mail: frank_hon@foxmail.com
 */
interface PlayerLifecycleObserver {

    /**
     * @param playerInfo current player info
     */
    fun onPlayerConnected(playerInfo: CurrentPlayerInfo?) {}

    fun onPrepare(song: SimpleSong, playMode: PlayMode, curIndex: Int, totalSize: Int) {}

    fun onPlaying(song: SimpleSong) {}

    fun onAudioPause() {}

    fun onAudioResume() {}

    fun onAudioStop() {}

    fun onCompleted() {}

    /**
     * 播放列表全部播放完毕
     */
    fun onFinished() {}

    fun onError(errorMsg: String) {}

}