package com.frankhon.fantasymusic.data.source.local

import com.frankhon.fantasymusic.utils.*
import com.frankhon.fantasymusic.vo.SimpleSong
import com.frankhon.fantasymusic.vo.bean.DataSong
import com.hon.mylogger.MyLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Frank Hon on 2022/9/7 7:04 下午.
 * E-mail: frank_hon@foxmail.com
 */
class LocalMusicDataSource(private val musicDao: MusicDao) {

    suspend fun getSongs(): List<SimpleSong> {
        var songs = musicDao.getSongs().transformToSimpleSongs()
        if (songs.isEmpty()) {
            MyLogger.d("From assets")
            songs = withContext(Dispatchers.IO) {
                getSongsFromAssets().onEach {
                    it.canDelete = false
                }
            }
            musicDao.insertSongs(songs.transformToDBSongs())
        } else {
            MyLogger.d("From db")
        }
        return songs
    }

    suspend fun insertSong(song: SimpleSong) {
        musicDao.insertSong(song.transformToDBSong())
    }

    suspend fun deleteSong(song: SimpleSong) {
        //将歌曲从数据库中删除
        val result = musicDao.deleteSong(song.transformToDBSong())
        if (result > 0) {
            //数据库中删除成功之后，将本地歌曲文件删除
            deleteFile(song)
        }
    }
}