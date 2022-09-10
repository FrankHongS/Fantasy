package com.frankhon.fantasymusic.data.source

import androidx.lifecycle.LiveData
import com.frankhon.fantasymusic.data.source.local.LocalMusicDataSource
import com.frankhon.fantasymusic.vo.SimpleSong

/**
 * Created by Frank Hon on 2022/9/7 7:05 下午.
 * E-mail: frank_hon@foxmail.com
 */
class MusicRepository(private val localDataSource: LocalMusicDataSource) {

    suspend fun getSongs(): List<SimpleSong> {
        return localDataSource.getSongs()
    }

}