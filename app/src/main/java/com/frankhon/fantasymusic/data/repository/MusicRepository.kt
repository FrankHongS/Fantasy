package com.frankhon.fantasymusic.data.repository

import android.net.Uri
import com.frankhon.fantasymusic.data.source.local.LocalMusicDataSource
import com.frankhon.fantasymusic.data.source.remote.RemoteMusicDataSource
import com.frankhon.fantasymusic.utils.*
import com.frankhon.fantasymusic.vo.SimpleSong

/**
 * Created by Frank Hon on 2022/9/7 7:05 下午.
 * E-mail: frank_hon@foxmail.com
 */
class MusicRepository(
    private val localDataSource: LocalMusicDataSource,
    private val remoteDataSource: RemoteMusicDataSource
) {

    suspend fun getSongs(): List<SimpleSong> {
        return localDataSource.getSongs().transformToSimpleSongs()
    }

    suspend fun insertSong(song: SimpleSong) {
        song.run {
            val dbSong = transformToDBSong()
            if (lyricsUri.isNullOrEmpty()) {
                val lyrics = remoteDataSource.getLyrics(cid).data?.lyrics
                if (!lyrics.isNullOrEmpty()) {
                    val lyricsFile = writeStringToPath(
                        content = lyrics,
                        path = getLyricsPath(),
                        fileName = getLyricsFileName(name, artist)
                    )
                    lyricsFile?.let {
                        val uriStr = Uri.fromFile(it).toString()
                        dbSong.lyricsUri = uriStr
                        this.lyricsUri = uriStr
                    }
                }
            }
            localDataSource.insertSong(dbSong)
        }
    }

    suspend fun deleteSong(song: SimpleSong) {
        return localDataSource.deleteSong(song.transformToDBSong())
    }
}