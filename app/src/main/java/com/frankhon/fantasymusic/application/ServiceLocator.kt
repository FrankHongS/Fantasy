package com.frankhon.fantasymusic.application

import androidx.room.Room
import com.frankhon.fantasymusic.utils.APP_NAME
import com.frankhon.fantasymusic.data.source.MusicRepository
import com.frankhon.fantasymusic.data.source.local.LocalMusicDataSource
import com.frankhon.fantasymusic.data.source.local.MusicDatabase

/**
 * Created by Frank Hon on 2022/9/7 10:10 下午.
 * E-mail: frank_hon@foxmail.com
 */
object ServiceLocator {

    private val musicDatabase = Room.databaseBuilder(
        Fantasy.getAppContext(),
        MusicDatabase::class.java,
        "$APP_NAME.db"
    )
        .fallbackToDestructiveMigration()
        .build()

    fun provideMusicRepository(): MusicRepository {
        return MusicRepository(LocalMusicDataSource(musicDatabase.musicDao))
    }

}