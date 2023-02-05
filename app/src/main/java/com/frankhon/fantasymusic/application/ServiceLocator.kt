package com.frankhon.fantasymusic.application

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.frankhon.fantasymusic.data.repository.ArtistRepository
import com.frankhon.fantasymusic.data.repository.MusicRepository
import com.frankhon.fantasymusic.data.repository.SearchRepository
import com.frankhon.fantasymusic.data.source.local.LocalArtistDataSource
import com.frankhon.fantasymusic.data.source.local.LocalMusicDataSource
import com.frankhon.fantasymusic.data.db.MusicDatabase
import com.frankhon.fantasymusic.data.repository.AlbumRepository
import com.frankhon.fantasymusic.data.source.local.LocalAlbumDataSource
import com.frankhon.fantasymusic.data.source.remote.RemoteMusicDataSource
import com.frankhon.fantasymusic.utils.APP_NAME

/**
 * Created by Frank Hon on 2022/9/7 10:10 下午.
 * E-mail: frank_hon@foxmail.com
 */
object ServiceLocator {

    //region Manual migrations 手动迁移数据库
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            //sql的TEXT类型默认可空的（String?），若要指定非空，需加上NOT NULL
            database.execSQL("ALTER TABLE `songs` ADD COLUMN `lyrics_uri` TEXT")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            //sql的TEXT类型默认可空的（String?），若要指定非空，需加上NOT NULL
            database.execSQL("ALTER TABLE `songs` ADD COLUMN `cid` TEXT")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `songs` ADD COLUMN `created_at` INTEGER NOT NULL default 1")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `songs` ADD COLUMN `album_name` TEXT")
        }
    }

    //endregion

    private val musicDatabase = Room.databaseBuilder(
        Fantasy.appContext,
        MusicDatabase::class.java,
        "$APP_NAME.db"
    )
        .addMigrations(MIGRATION_1_2)
        .addMigrations(MIGRATION_2_3)
        .addMigrations(MIGRATION_3_4)
        .addMigrations(MIGRATION_4_5)
        .fallbackToDestructiveMigration()
        .build()

    fun provideMusicRepository(): MusicRepository {
        return MusicRepository(provideLocalDataSource(), provideRemoteMusicDataSource())
    }

    fun provideSearchRepository(): SearchRepository {
        return SearchRepository(provideRemoteMusicDataSource(), provideLocalDataSource())
    }

    fun provideArtistRepository(): ArtistRepository {
        return ArtistRepository(LocalArtistDataSource(musicDatabase.artistsDao))
    }

    fun provideAlbumRepository(): AlbumRepository {
        return AlbumRepository(LocalAlbumDataSource(musicDatabase.albumDao))
    }

    private fun provideLocalDataSource(): LocalMusicDataSource {
        return LocalMusicDataSource(musicDatabase.musicDao)
    }

    private fun provideRemoteMusicDataSource(): RemoteMusicDataSource {
        return RemoteMusicDataSource()
    }
}