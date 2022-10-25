package com.frankhon.fantasymusic.ui.fragments.search

import com.frankhon.fantasymusic.vo.SimpleSong

/**
 * 对正在下载歌曲进行管理
 *
 * Created by Frank Hon on 2022/10/30 9:46 下午.
 * E-mail: frank_hon@foxmail.com
 */
object SongDownloadManager {

    private val toDownloadSongs = mutableListOf<SimpleSong>()

    fun addSong(song: SimpleSong) {
        if (!toDownloadSongs.contains(song)) {
            toDownloadSongs.add(song)
        }
    }

    fun removeSong(song: SimpleSong) {
        toDownloadSongs.remove(song)
    }

    fun contains(song: SimpleSong) = toDownloadSongs.contains(song)

    fun contains(name: String?, artist: String?): Boolean {
        toDownloadSongs.forEach {
            if (it.name == name && it.artist == artist) {
                return true
            }
        }
        return false
    }
}