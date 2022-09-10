package com.frankhon.fantasymusic.vo.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.frankhon.fantasymusic.vo.SimpleSong

/**
 * Created by Frank Hon on 2022/9/9 3:44 下午.
 * E-mail: frank_hon@foxmail.com
 */
@Entity(tableName = "songs", primaryKeys = ["song_name", "song_artist"])
data class DBSong(
    @ColumnInfo(name = "song_name")
    val name: String,
    @ColumnInfo(name = "song_artist")
    val artist: String,
    @ColumnInfo(name = "song_uri")
    val songUri: String,
    @ColumnInfo(name = "pic_url")
    val picUrl: String
)

