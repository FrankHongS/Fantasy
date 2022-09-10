package com.frankhon.fantasymusic.data.source.local

import com.frankhon.fantasymusic.utils.FileUtil
import com.frankhon.fantasymusic.utils.transformToSimpleSong
import com.frankhon.fantasymusic.vo.SimpleSong
import com.hon.mylogger.MyLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Frank Hon on 2022/9/7 7:04 下午.
 * E-mail: frank_hon@foxmail.com
 */
class LocalMusicDataSource(private val musicDao: MusicDao) {

    suspend fun getSongs(): List<SimpleSong> {
        var songs = musicDao.getSongs().map { it.transformToSimpleSong() }
        if (songs.isEmpty()) {
            MyLogger.d("From assets")
            songs = withContext(Dispatchers.IO) {
                FileUtil.getSongsFromAssets()
            }
            musicDao.insertSongs(songs.map { it.transformToSimpleSong() })
        } else {
            MyLogger.d("From db")
        }
        return songs
    }

}