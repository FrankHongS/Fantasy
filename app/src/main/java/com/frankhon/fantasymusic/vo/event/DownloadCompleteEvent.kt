package com.frankhon.fantasymusic.vo.event

import com.frankhon.fantasymusic.vo.SimpleSong

/**
 * 歌曲下载完成事件
 * Created by Frank Hon on 2022/10/27 10:33 下午.
 * E-mail: frank_hon@foxmail.com
 */
data class DownloadCompleteEvent(val song: SimpleSong)