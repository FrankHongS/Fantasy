package com.frankhon.fantasymusic.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.frankhon.fantasymusic.vo.db.DBSong

/**
 * Created by Frank Hon on 2022/9/9 5:56 下午.
 * E-mail: frank_hon@foxmail.com
 */
@Database(
    entities = [DBSong::class],
    version = 5,
    exportSchema = true,
//    autoMigrations = [
//        AutoMigration(from = 1, to = 2)
//    ]
)
abstract class MusicDatabase : RoomDatabase() {

    abstract val musicDao: MusicDao

    abstract val artistsDao: ArtistDao

    abstract val albumDao: AlbumDao

}