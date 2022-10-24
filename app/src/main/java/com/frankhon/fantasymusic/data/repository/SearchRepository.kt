package com.frankhon.fantasymusic.data.repository

import com.frankhon.fantasymusic.data.Result
import com.frankhon.fantasymusic.data.source.local.LocalMusicDataSource
import com.frankhon.fantasymusic.data.source.remote.RemoteMusicDataSource
import com.frankhon.fantasymusic.vo.bean.DataSongWrapper

/**
 * Created by Frank Hon on 2022/9/24 11:33 上午.
 * E-mail: frank_hon@foxmail.com
 */
class SearchRepository(
    private val remoteDataSource: RemoteMusicDataSource,
    private val localDataSource: LocalMusicDataSource
) {

    suspend fun findSong(keyword: String): Result<DataSongWrapper> {
        val result = remoteDataSource.findSong(keyword)
        return result.apply {
            if (isSuccess) {
                data?.data?.let {
                    val localSongs = localDataSource.getSongs()
                    //替换网络获取的列表中的本地歌曲
                    it.songs.forEach { song ->
                        localSongs.forEach { localSong ->
                            if (!song.artists.isNullOrEmpty()) {
                                if (song.name == localSong.name &&
                                    song.artists?.first()?.name == localSong.artist
                                ) {
                                    song.url = localSong.location
                                }
                            } else {
                                if (song.name == localSong.name &&
                                    localSong.artist.isNullOrEmpty()
                                ) {
                                    song.url = localSong.location
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}