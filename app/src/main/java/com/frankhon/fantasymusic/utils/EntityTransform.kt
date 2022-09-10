package com.frankhon.fantasymusic.utils

import com.frankhon.fantasymusic.vo.SimpleSong
import com.frankhon.fantasymusic.vo.db.DBSong

/**
 * Created by Frank Hon on 2022/9/9 6:27 下午.
 * E-mail: frank_hon@foxmail.com
 */

fun DBSong.transformToSimpleSong() = SimpleSong(
    name = name,
    artist = artist,
    location = songUri,
    songPic = picUrl
)

fun SimpleSong.transformToSimpleSong() = DBSong(
    name = name.orEmpty(),
    artist = artist.orEmpty(),
    songUri = location.orEmpty(),
    picUrl = songPic.orEmpty()
)