package com.frankhon.fantasymusic.data.source.remote

import com.frankhon.fantasymusic.BuildConfig
import com.frankhon.fantasymusic.utils.BASE_URL
import com.frankhon.fantasymusic.vo.bean.DataSongWrapper
import com.frankhon.fantasymusic.vo.bean.SingleSongWrapper
import com.frankhon.fantasymusic.vo.bean.SongLyricsWrapper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Frank Hon on 2020-06-01 23:45.
 * E-mail: frank_hon@foxmail.com
 */
interface MusicService {

    @GET("search")
    suspend fun findSong(@Query("keyword") keyword: String): Response<DataSongWrapper>

    @GET("song/url")
    suspend fun getSingleSongUrl(@Query("cid") cid: String?): Response<SingleSongWrapper>

    @GET("lyric")
    suspend fun getLyrics(@Query("cid") cid: String?): Response<SongLyricsWrapper>

    companion object {
        fun create(): MusicService {
            val logger = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MusicService::class.java)
        }
    }

}