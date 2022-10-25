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
                    val newData = it.songs.map { song ->
                        localSongs.forEach { localSong ->
                            if (song.name == localSong.name &&
                                song.artist == localSong.artist
                            ) {
                                song.url = localSong.songUri
                            }
                        }
                        song
                    }
                        .filter { song ->
                            //过滤掉非法Url(?表示匹配前面字符0或1次)
                            song.url?.matches(Regex("^(https?://|file://).*")) == true
                        }
                    it.songs = newData
                }
            }
        }
    }

}