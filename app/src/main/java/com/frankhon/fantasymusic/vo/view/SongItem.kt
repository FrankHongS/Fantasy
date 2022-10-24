package com.frankhon.fantasymusic.vo.view

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SongItem

        if (name != other.name) return false
        if (artist != other.artist) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (artist?.hashCode() ?: 0)
        return result
    }
}
