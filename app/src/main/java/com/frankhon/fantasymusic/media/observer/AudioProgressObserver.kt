package com.frankhon.fantasymusic.media.observer

/**
 * Created by Frank Hon on 2022/8/29 2:34 下午.
 * E-mail: frank_hon@foxmail.com
 */
interface AudioProgressObserver {

    fun onProgressUpdated(curPosition: Long, duration: Long)

}