package com.frankhon.fantasymusic.media

import androidx.core.util.lruCache
import com.frankhon.fantasymusic.media.observer.PlayerLifecycleObserver
import com.frankhon.fantasymusic.utils.parseLyricsFile
import com.frankhon.fantasymusic.vo.CurrentPlayerInfo
import com.frankhon.fantasymusic.vo.SimpleSong
import com.hon.mylogger.MyLogger
import kotlinx.coroutines.*
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Frank Hon on 2022/11/9 9:09 下午.
 * E-mail: frank_hon@foxmail.com
 */
object LyricsManager : PlayerLifecycleObserver {

    private val mainScope by lazy { MainScope() }
    private var lyricListJob: Job? = null

    private val lyricsCache by lazy {
        // 内部LinkedHashMap实现
        lruCache<SimpleSong, List<Pair<Long, String>>>(
            maxSize = 6,
            create = {
                parseLyricsFile(it.lyricsUri)
            }
        )
    }

    private var curLyrics: List<Pair<Long, String>>? = null
    private var curLyricsIndex = 0

    fun init() {
        AudioPlayerManager.registerLifecycleObserver(this)
    }

    fun compareAndGetLyric(progress: Long): String? {
        curLyrics?.let {
            // region 定位歌词，跳过中间未显示的歌词，直接显示当前播放的歌词
            val curLyricTime = it[curLyricsIndex].first
            // 当向后拖拽进度条或退出页面再进入，阈值为2s(当前进度-当前歌词时间)
            if (progress - curLyricTime >= 2_000) {
                for (i in curLyricsIndex until it.size) {
                    val lyricTime = it[i].first
                    if (lyricTime > progress) {
                        curLyricsIndex = max(curLyricsIndex, i - 1)
                        break
                    } else if (lyricTime == progress) {
                        curLyricsIndex = i
                        break
                    } else {
                        // 当所有的歌词时间都小于进度，显示最后一句歌词
                        if (i == it.size - 1) {
                            curLyricsIndex = i
                            break
                        }
                    }
                }
            }
            // 当向前拖拽进度条
            else if (progress < curLyricTime) {
                for (i in it.indices) {
                    val lyricTime = it[i].first
                    if (lyricTime > progress) {
                        if (i == 0) {
                            curLyricsIndex = 0
                            return ""
                        } else {
                            curLyricsIndex = i - 1
                            break
                        }
                    }
                }
            }
            //endregion
            val (lyricTime, lyricContent) = it[curLyricsIndex]
            if (lyricTime <= progress) {
                curLyricsIndex = min(curLyricsIndex + 1, it.size - 1)
                return lyricContent
            }
        }
        return null
    }

    fun release() {
        lyricsCache.evictAll()
        lyricListJob?.cancel()
        reset()
    }

    override fun onPlayerConnected(playerInfo: CurrentPlayerInfo?) {
        MyLogger.d("onPlayerConnected: ${playerInfo?.curSong}")
        playerInfo?.curSong?.let {
            getLyricsFromCache(it)
        }
    }

    override fun onPrepare(song: SimpleSong, playMode: PlayMode, curIndex: Int, totalSize: Int) {
        reset()
        getLyricsFromCache(song)
    }

    private fun reset() {
        curLyrics = null
        curLyricsIndex = 0
    }

    private fun getLyricsFromCache(it: SimpleSong): Job {
        lyricListJob?.cancel()
        lyricListJob = Job()
        return mainScope.launch(lyricListJob!!) {
            withContext(Dispatchers.IO) {
                curLyrics = lyricsCache.get(it)
            }
        }
    }

}