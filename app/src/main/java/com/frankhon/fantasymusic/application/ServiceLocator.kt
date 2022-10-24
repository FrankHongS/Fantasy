package com.frankhon.fantasymusic.application

import androidx.room.Room
import com.frankhon.fantasymusic.utils.APP_NAME
import com.frankhon.fantasymusic.data.repository.MusicRepository
import com.frankhon.fantasymusic.data.repository.SearchRepository
import com.frankhon.fantasymusic.data.source.local.LocalMusicDataSource
import com.frankhon.fantasymusic.data.source.local.MusicDatabase
import com.frankhon.fantasymusic.data.source.remote.RemoteMusicDataSource

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
        return MusicRepository(provideLocalDataSource())
    }

    fun provideSearchRepository(): SearchRepository {
        return SearchRepository(RemoteMusicDataSource(), provideLocalDataSource())
    }

    fun provideLocalDataSource(): LocalMusicDataSource {
        return LocalMusicDataSource(musicDatabase.musicDao)
    }
}