package com.frankhon.fantasymusic.vo.db

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Created by Frank Hon on 2022/9/9 3:44 下午.
 * E-mail: frank_hon@foxmail.com
 */
@Entity(tableName = "songs", primaryKeys = ["song_name", "song_artist"])
data class DBSong(
    val cid: String? = "",
    @ColumnInfo(name = "song_name")
    val name: String,
    @ColumnInfo(name = "song_artist")
    val artistName: String,
    @ColumnInfo(name = "song_uri")
    val songUri: String,
    @ColumnInfo(name = "lyrics_uri")
    var lyricsUri: String? = "",
    @ColumnInfo(name = "pic_url")
    val albumCover: String,
    @ColumnInfo(name = "album_name")
    val albumName: String? = "",
    //是否可以删除，内置歌曲不支持删除
    @ColumnInfo(name = "able_to_delete")
    var canDelete: Boolean = true,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

