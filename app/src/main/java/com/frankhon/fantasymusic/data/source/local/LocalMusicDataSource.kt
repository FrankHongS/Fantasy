package com.frankhon.fantasymusic.data.source.local

import com.frankhon.fantasymusic.utils.*
import com.frankhon.fantasymusic.vo.SimpleSong
import com.frankhon.fantasymusic.vo.bean.DataSong
import com.frankhon.fantasymusic.vo.db.DBSong
import com.hon.mylogger.MyLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Frank Hon on 2022/9/7 7:04 下午.
 * E-mail: frank_hon@foxmail.com
 */
class LocalMusicDataSource(private val musicDao: MusicDao) {

    suspend fun getSongsByPage(limit: Int, offset: Int): List<DBSong> {
        var songs = musicDao.getSongs(limit, offset)
        if (songs.isEmpty() && offset == 0) {
            MyLogger.d("From assets")
            songs = withContext(Dispatchers.IO) {
                getSongsFromAssets()
            }
            musicDao.insertSongs(songs.onEach { it.canDelete = false })
        } else {
            MyLogger.d("From db")
        }
        return songs
    }

    suspend fun getSongs(): List<DBSong> {
        var songs = musicDao.getSongs()
        if (songs.isEmpty()) {
            songs = withContext(Dispatchers.IO) {
                getSongsFromAssets()
            }
            musicDao.insertSongs(songs.onEach { it.canDelete = false })
        }
        return songs
    }

    suspend fun insertSong(song: DBSong) {
        musicDao.insertSong(song)
    }

    suspend fun deleteSong(song: DBSong) {
        //将歌曲从数据库中删除
        val result = musicDao.deleteSong(song)
        if (result > 0) {
            //数据库中删除成功之后，将本地歌曲文件删除
            deleteFile(song)
        }
    }

    suspend fun getCount(): Int {
        return musicDao.getCount()
    }
}