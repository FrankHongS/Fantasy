package com.frankhon.fantasymusic.api

import com.frankhon.fantasymusic.BuildConfig
import com.frankhon.fantasymusic.vo.Song
import com.frankhon.fantasymusic.vo.SongWrapper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
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

    @GET("song/find")
    suspend fun findSong(@Query("keyword") keyword: String): Response<SongWrapper>

    companion object {
        private const val BASE_URL = "http://192.168.124.2:3400/"

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