package com.frankhon.fantasymusic.utils

import com.frankhon.fantasymusic.ui.fragments.search.SongDownloadManager
import com.frankhon.fantasymusic.vo.SimpleSong
import com.frankhon.fantasymusic.vo.bean.DataSong
import com.frankhon.fantasymusic.vo.db.DBSong
import com.frankhon.fantasymusic.vo.view.SearchSongItem
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
            songUri = it.songUri,
            picUrl = it.picUrl,
            canDelete = it.canDelete
        )
    }
}

fun List<SimpleSong>.transformToDBSongs(): List<DBSong> {
    return map {
        DBSong(
            name = it.name.orEmpty(),
            artist = it.artist.orEmpty(),
            songUri = it.songUri.orEmpty(),
            picUrl = it.picUrl.orEmpty(),
            canDelete = it.canDelete
        )
    }
}

fun List<SimpleSong>.transferToSongItems(playingIndex: Int = -1): List<SongItem> {
    return mapIndexed { index, item ->
        SongItem(
            name = item.name,
            artist = item.artist,
            songPic = item.picUrl,
            isPlaying = index == playingIndex
        )
    }
}

fun List<DataSong>.transferToSearchSongItems(): List<SearchSongItem> {
    return map {
        SearchSongItem(
            name = it.name,
            artist = it.artist,
            albumPicUrl = it.albumPicUrl,
            songUri = it.url,
            downloadState = if (it.url?.startsWith("file://") == true) {
                2
            } else if (SongDownloadManager.contains(it.name, it.artist)) {
                1
            } else {
                0
            }
        )
    }
}

/**
 * 只需要name和artist，用作数据库查询
 */
fun SongItem.transformToSimpleSong(): SimpleSong {
    return SimpleSong(
        name = name,
        artist = artist
    )
}

fun SimpleSong.transformToDBSong(): DBSong {
    return DBSong(
        name = name.orEmpty(),
        artist = artist.orEmpty(),
        songUri = songUri.orEmpty(),
        picUrl = picUrl.orEmpty()
    )
}

fun DataSong.transformToSimpleSong(): SimpleSong {
    return SimpleSong(
        name = name.orEmpty(),
        artist = artists?.first()?.name.orEmpty(),
        songUri = url.orEmpty(),
        picUrl = album?.picUrl.orEmpty()
    )
}

fun SearchSongItem.transformToSimpleSong() = SimpleSong(
    name = name.orEmpty(),
    artist = artist.orEmpty(),
    songUri = songUri.orEmpty(),
    picUrl = albumPicUrl.orEmpty()
)