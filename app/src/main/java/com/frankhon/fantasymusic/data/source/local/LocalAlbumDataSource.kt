package com.frankhon.fantasymusic.data.source.local

import com.frankhon.fantasymusic.data.db.AlbumDao
import com.frankhon.fantasymusic.vo.db.DBAlbum

/**
 * Created by shuaihua_a on 2023/1/14 15:32.
 * E-mail: hongshuaihua
 */
class LocalAlbumDataSource(private val albumDao: AlbumDao) {

    suspend fun getSongInfoByAlbum(): List<DBAlbum> {
        return albumDao.getSongInfoByAlbum()
    }

}