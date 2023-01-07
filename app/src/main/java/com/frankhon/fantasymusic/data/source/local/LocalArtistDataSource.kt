package com.frankhon.fantasymusic.data.source.local

import com.frankhon.fantasymusic.data.db.ArtistDao
import com.frankhon.fantasymusic.vo.db.DBArtist

/**
 * Created by Frank Hon on 2023/1/8 1:02 下午.
 * E-mail: frank_hon@foxmail.com
 */
class LocalArtistDataSource(private val artistsDao: ArtistDao) {

    suspend fun getSongInfoByArtist(): List<DBArtist> {
        return artistsDao.getSongInfoByArtist()
    }

}