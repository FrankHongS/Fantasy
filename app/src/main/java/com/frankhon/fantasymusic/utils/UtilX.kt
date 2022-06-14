package com.frankhon.fantasymusic.utils

import com.frankhon.fantasymusic.Fantasy

/**
 * Created by Frank Hon on 2022/6/14 11:18 下午.
 * E-mail: frank_hon@foxmail.com
 */

fun dp2px(dp: Int): Int {
    val density = Fantasy.getAppContext().resources.displayMetrics.density
    return (dp * density + 0.5f).toInt()
}

/**
 * 扩展属性，DSL
 */
val Int.dp: Int
    get() {
        return dp2px(this)
    }

fun getSystemService(name: String): Any? {
    return Fantasy.getAppContext().getSystemService(name)
}