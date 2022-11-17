package com.frankhon.fantasymusic.media

/**
 * Created by Frank Hon on 2022/6/15 7:24 下午.
 * E-mail: frank_hon@foxmail.com
 */
enum class PlayerState() {
    IDLE,
    PREPARING,
    PLAYING,
    RESUMED,
    PAUSED,
    STOPPED,
    COMPLETED,

    /**
     * 播放列表全部播放完毕
     */
    FINISHED,
    ERROR;
}

fun PlayerState.isPlaying() = this == PlayerState.PLAYING || this == PlayerState.RESUMED

fun PlayerState.isStopped() = this == PlayerState.IDLE || this == PlayerState.STOPPED

fun PlayerState.isPlayingInNotification() =
    this == PlayerState.PLAYING || this == PlayerState.RESUMED
            || this == PlayerState.PREPARING || this == PlayerState.COMPLETED
