package com.frankhon.fantasymusic.media

import com.frankhon.fantasymusic.vo.SimpleSong

/**
 * Created by Frank Hon on 2022/6/14 10:43 下午.
 * E-mail: frank_hon@foxmail.com
 */
interface AudioLifecycleObserver {

    fun onPrepare(song: SimpleSong) {}

    fun onPlaying() {}

    fun onPause() {}

    fun onResume() {}

    fun onStop() {}

    fun onCompleted() {}

    fun onError() {}

    fun onProgressUpdated(curPosition: Int, duration: Int) {}
}