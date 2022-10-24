package com.frankhon.fantasymusic.utils

import com.frankhon.fantasymusic.vo.SimpleSong
import com.frankhon.fantasymusic.vo.bean.DataSong
import com.frankhon.fantasymusic.vo.db.DBSong
import com.frankhon.fantasymusic.vo.view.SongItem

/**
 * Created by Frank Hon on 2022/9/9 6:27 下午.
 * E-mail: frank_hon@foxmail.com
 */

fun List<DBSong>.transformToSimpleSongs(): List<SimpleSong> {
    return map {
        SimpleSong(
            name = it.name,
            artist = it.artist,
            location = it.songUri,
            songPic = it.picUrl
        )
    }
}

fun List<SimpleSong>.transformToDBSongs(): List<DBSong> {
    return map {
        DBSong(
            name = it.name.orEmpty(),
            artist = it.artist.orEmpty(),
            songUri = it.location.orEmpty(),
            picUrl = it.songPic.orEmpty()
        )
    }
}

fun List<SimpleSong>.transferToSongItems(playingIndex: Int = -1): List<SongItem> {
    return mapIndexed { index, item ->
        SongItem(
            name = item.name,
            artist = item.artist,
            songPic = item.songPic,
            isPlaying = index == playingIndex
        )
    }
}

fun DataSong.transformToDBSong(): DBSong {
    return DBSong(
        name = name.orEmpty(),
        artist = artists?.first()?.name.orEmpty(),
        songUri = url.orEmpty(),
        picUrl = album?.picUrl.orEmpty()
    )
}