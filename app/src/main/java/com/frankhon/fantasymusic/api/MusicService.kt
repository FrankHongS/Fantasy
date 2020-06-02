package com.frankhon.fantasymusic.api

import com.frankhon.fantasymusic.vo.Song
import com.frankhon.fantasymusic.vo.SongWrapper
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Frank Hon on 2020-06-01 23:45.
 * E-mail: frank_hon@foxmail.com
 */
interface MusicService {

    @GET("song/find")
    fun findSong(@Query("keyword") keyword: String): Call<SongWrapper>

}