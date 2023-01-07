package com.frankhon.fantasymusic.vo.db

import androidx.room.ColumnInfo

/**
 * Created by shuaihua_a on 2023/1/14 15:18.
 * E-mail: hongshuaihua
 */
data class DBAlbum(
    @ColumnInfo(name = "album_name")
    val name: String?,
    @ColumnInfo(name = "song_artist")
    val artistName: String,
    @ColumnInfo(name = "pic_url")
    val albumCover: String?,
    @ColumnInfo(name = "count(*)")
    val songsCount: Int
)
