package com.frankhon.fantasymusic.media

/**
 * Created by Frank Hon on 2022/6/14 10:43 下午.
 * E-mail: frank_hon@foxmail.com
 */
interface AudioLifecycleObserver {

    fun onPrepare() {}

    fun onPlaying() {}

    fun onPause() {}

    fun onResume() {}

    fun onStop() {}

    fun onCompleted() {}

    fun onError() {}
}