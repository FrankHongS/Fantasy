package com.frankhon.fantasymusic.media

/**
 * Created by Frank Hon on 2022/6/15 7:24 下午.
 * E-mail: frank_hon@foxmail.com
 */
enum class PlayerState {
    IDLE,
    PREPARING,
    PLAYING,
    RESUMED,
    TRANSIENT_PAUSED,  // passive pause
    PAUSED,
    COMPLETED,
    ERROR
}