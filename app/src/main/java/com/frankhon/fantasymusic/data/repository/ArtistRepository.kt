package com.frankhon.fantasymusic.data.repository

import com.frankhon.fantasymusic.data.source.local.LocalArtistDataSource
import com.frankhon.fantasymusic.vo.db.DBArtist

/**
 * Created by Frank Hon on 2023/1/8 1:08 下午.
 * E-mail: frank_hon@foxmail.com
 */
class ArtistRepository(
    private val localArtistDataSource: LocalArtistDataSource
) {

    suspend fun getSongInfoByArtist(): List<DBArtist> {
        return localArtistDataSource.getSongInfoByArtist()
    }

}