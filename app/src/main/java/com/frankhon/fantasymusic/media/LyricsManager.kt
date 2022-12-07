package com.frankhon.fantasymusic.media

import androidx.core.util.lruCache
import com.frankhon.fantasymusic.media.observer.PlayerLifecycleObserver
import com.frankhon.fantasymusic.utils.parseLyricsFile
import com.frankhon.fantasymusic.vo.SimpleSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

/**
 * Lyrics管理类
 *
 * Created by Frank Hon on 2022/11/9 9:09 下午.
 * E-mail: frank_hon@foxmail.com
 */
object LyricsManager : PlayerLifecycleObserver {

    private var lyricListJob: Job? = null

    private val lyricsCache by lazy {
        // 内部使用LinkedHashMap实现
        lruCache<SimpleSong, List<Pair<Long, String>>>(
            maxSize = 6,
            create = {
                parseLyricsFile(it.lyricsUri)
            }
        )
    }

    private var curLyrics: List<Pair<Long, String>>? = null

    fun getLyricText(progress: Long): String? {
        curLyrics?.takeIf { it.isNotEmpty() }?.let {
            val (lastLyricsTime, lastLyric) = it.last()
            if (progress >= lastLyricsTime) {
                return lastLyric
            }
            it.forEachIndexed { index, (lyricTime, _) ->
                if (progress < lyricTime) {
                    return if (index < 1) {
                        null
                    } else {
                        it[index - 1].second
                    }
                }
            }
        }
        return null
    }

    suspend fun loadLyrics(song: SimpleSong): List<Pair<Long, String>>? {
        return withContext(Dispatchers.IO) {
            curLyrics = lyricsCache.get(song)
            curLyrics
        }
    }

    fun release() {
        lyricsCache.evictAll()
        lyricListJob?.cancel()
        curLyrics = null
    }

}