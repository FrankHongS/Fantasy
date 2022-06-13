package com.frankhon.fantasymusic.data

import com.frankhon.fantasymusic.api.MusicService
import com.frankhon.fantasymusic.api.Result
import com.frankhon.fantasymusic.vo.SongWrapper

/**
 * Created by Frank Hon on 2022/2/12 7:37 下午.
 * E-mail: frank_hon@foxmail.com
 */
object MusicSource {

    suspend fun findSong(keyword: String): Result<SongWrapper> {

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