package com.frankhon.fantasymusic.data.repository

import com.frankhon.fantasymusic.data.source.local.LocalAlbumDataSource
import com.frankhon.fantasymusic.vo.db.DBAlbum

/**
 * Created by Frank Hon on 2023/1/8 1:08 下午.
 * E-mail: frank_hon@foxmail.com
 */
class AlbumRepository(
    private val localAlbumDataSource: LocalAlbumDataSource
) {

    suspend fun getSongInfoByAlbum(): List<DBAlbum> {
        return localAlbumDataSource.getSongInfoByAlbum()
    }

}