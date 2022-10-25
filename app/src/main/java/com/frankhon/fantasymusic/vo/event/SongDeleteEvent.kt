package com.frankhon.fantasymusic.vo.event

import com.frankhon.fantasymusic.vo.SimpleSong

/**
 * 歌曲删除事件
 *
 * Created by Frank Hon on 2022/10/28 1:25 下午.
 * E-mail: frank_hon@foxmail.com
 */
data class SongDeleteEvent(val song: SimpleSong)