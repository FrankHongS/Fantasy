package com.frankhon.fantasymusic.data.source.remote

import com.frankhon.fantasymusic.data.Result
import com.frankhon.fantasymusic.vo.bean.DataSongWrapper
import com.frankhon.fantasymusic.vo.bean.SingleSongWrapper
import com.frankhon.fantasymusic.vo.bean.SongLyricsWrapper
import retrofit2.Response

/**
 * Created by Frank Hon on 2022/2/12 7:37 下午.
 * E-mail: frank_hon@foxmail.com
 */
class RemoteMusicDataSource {

    private val musicService by lazy { MusicService.create() }

    suspend fun findSong(keyword: String): Result<DataSongWrapper> {
        return generateResult {
            musicService.findSong(keyword)
        }
    }

    /**
     * 一些歌曲资源需要这个api单独获取
     */
    suspend fun getSingleSongUrl(cid: String?): Result<SingleSongWrapper> {
        return generateResult {
            musicService.getSingleSongUrl(cid)
        }
    }

    suspend fun getLyrics(cid: String?): Result<SongLyricsWrapper> {
        return generateResult {
            musicService.getLyrics(cid)
        }
    }

    private suspend fun <T> generateResult(request: suspend () -> Response<T>): Result<T> {
        return try {
            val response = request()
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