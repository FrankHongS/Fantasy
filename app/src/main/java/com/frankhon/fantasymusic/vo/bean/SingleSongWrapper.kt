package com.frankhon.fantasymusic.vo.bean

import com.google.gson.annotations.SerializedName

/**
 * Created by Frank Hon on 2022/11/8 7:32 下午.
 * E-mail: frank_hon@foxmail.com
 */
data class SingleSongWrapper(
    val result: Int,
    @SerializedName("data")
    val songUrl: String
)
