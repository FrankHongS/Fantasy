package com.frankhon.fantasymusic.vo.view

import com.frankhon.fantasymusic.vo.SimpleSong

/**
 * Created by Frank Hon on 2022/8/29 7:14 下午.
 * E-mail: frank_hon@foxmail.com
 */
data class SongItem(
    val name: String?,
    val artist: String?,
    val songPic: String? = "",
    var isPlaying: Boolean = false
) {
    fun clone(): SongItem {
        return SongItem(name, artist, songPic, isPlaying)
    }
}
