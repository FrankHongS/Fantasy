package com.frankhon.fantasymusic.vo.db

import androidx.room.ColumnInfo

/**
 * Created by Frank Hon on 2023/1/8 12:56 下午.
 * E-mail: frank_hon@foxmail.com
 */
data class DBArtist(
    @ColumnInfo(name = "song_artist")
    val name: String,
    @ColumnInfo(name = "pic_url")
    val albumCover: String?,
    @ColumnInfo(name = "count(*)")
    val songsCount: Int
)
