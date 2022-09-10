package com.frankhon.fantasymusic.data.source.remote

import com.frankhon.fantasymusic.data.Result
import com.frankhon.fantasymusic.vo.bean.DataSongWrapper

/**
 * Created by Frank Hon on 2022/2/12 7:37 下午.
 * E-mail: frank_hon@foxmail.com
 */
object RemoteMusicDataSource {

    suspend fun findSong(keyword: String): Result<DataSongWrapper> {

        return try {
            val response = MusicService.create().findSong(keyword)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(response.errorBody()?.toString())
            }
        } catch (e: Exception) {
            Result.failure(e.message)
        }
    }

}