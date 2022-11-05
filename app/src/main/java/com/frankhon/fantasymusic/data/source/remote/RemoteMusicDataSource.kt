package com.frankhon.fantasymusic.data.source.remote

import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.data.Result
import com.frankhon.fantasymusic.utils.getString
import com.frankhon.fantasymusic.vo.bean.DataSongWrapper

/**
 * Created by Frank Hon on 2022/2/12 7:37 下午.
 * E-mail: frank_hon@foxmail.com
 */
class RemoteMusicDataSource {

    suspend fun findSong(keyword: String): Result<DataSongWrapper> {
        if (keyword.isEmpty()) {
            return Result.failure(getString(R.string.search_input_empty))
        }
        return try {
            val response = MusicService.create().findSong(keyword)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.data?.songs.isNullOrEmpty()) {
                    Result.failure(getString(R.string.search_result_empty))
                } else {
                    Result.success(body)
                }
            } else {
                Result.failure(response.errorBody()?.toString())
            }
        } catch (e: Exception) {
            Result.failure(e.message)
        }
    }

}