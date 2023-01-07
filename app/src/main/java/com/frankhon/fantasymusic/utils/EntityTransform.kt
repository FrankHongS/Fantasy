package com.frankhon.fantasymusic.utils

import com.frankhon.fantasymusic.ui.fragments.search.SongDownloadManager
import com.frankhon.fantasymusic.vo.SimpleSong
import com.frankhon.fantasymusic.vo.bean.DataSong
import com.frankhon.fantasymusic.vo.db.DBAlbum
import com.frankhon.fantasymusic.vo.db.DBArtist
import com.frankhon.fantasymusic.vo.db.DBSong
import com.frankhon.fantasymusic.vo.view.AlbumItem
import com.frankhon.fantasymusic.vo.view.ArtistItem
import com.frankhon.fantasymusic.vo.view.SearchSongItem
import com.frankhon.fantasymusic.vo.view.SongItem

/**
 * Created by Frank Hon on 2022/9/9 6:27 下午.
 * E-mail: frank_hon@foxmail.com
 */

fun List<DBSong>.transformToSimpleSongs(): List<SimpleSong> {
    return map {
        SimpleSong(
            cid = it.cid,
            name = it.name,
            artist = it.artistName,
            songUri = it.songUri,
            lyricsUri = it.lyricsUri,
            picUrl = it.albumCover,
            canDelete = it.canDelete
        )
    }
}

fun List<SimpleSong>.transformToDBSongs(): List<DBSong> {
    return map {
        DBSong(
            cid = it.cid.orEmpty(),
            name = it.name.orEmpty(),
            artistName = it.artist.orEmpty(),
            songUri = it.songUri.orEmpty(),
            albumCover = it.picUrl.orEmpty(),
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
            cid = it.cid,
            name = it.name,
            artist = it.artist,
            albumPicUrl = it.albumPicUrl,
            albumName = it.albumName,
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

fun List<DBArtist>.transferToArtistItems(): List<ArtistItem> {
    return map {
        ArtistItem(
            name = it.name,
            albumCover = it.albumCover,
            songsCount = it.songsCount
        )
    }
}

fun List<DBAlbum>.transferToAlbumItems(): List<AlbumItem> {
    return map {
        AlbumItem(
            name = it.name,
            artistName = it.artistName,
            albumCover = it.albumCover,
            songsCount = it.songsCount
        )
    }
}

fun SimpleSong.transformToDBSong(): DBSong {
    return DBSong(
        cid = cid.orEmpty(),
        name = name.orEmpty(),
        artistName = artist.orEmpty(),
        songUri = songUri.orEmpty(),
        lyricsUri = lyricsUri.orEmpty(),
        albumCover = picUrl.orEmpty(),
        albumName = albumName,
        canDelete = canDelete
    )
}

fun SearchSongItem.transformToSimpleSong() = SimpleSong(
    cid = cid,
    name = name.orEmpty(),
    artist = artist.orEmpty(),
    songUri = songUri.orEmpty(),
    lyricsUri = lyricsUri.orEmpty(),
    picUrl = albumPicUrl.orEmpty(),
    albumName = albumName
)

fun SongItem.compareTo(song: SimpleSong?): Boolean {
    return song?.let {
        name == it.name && artist == it.artist
    } ?: kotlin.run {
        false
    }
}
