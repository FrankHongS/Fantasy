package com.frankhon.fantasymusic.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.frankhon.fantasymusic.vo.db.DBSong

/**
 * Created by Frank Hon on 2022/9/9 3:38 下午.
 * E-mail: frank_hon@foxmail.com
 */
@Dao
interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: DBSong)

    suspend fun insertSongs(songs: List<DBSong>) {
        songs.forEach { insertSong(it) }
    }

    @Query("select * from songs")
    suspend fun getSongs(): List<DBSong>

}