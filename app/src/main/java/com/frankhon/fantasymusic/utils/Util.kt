package com.frankhon.fantasymusic.utils

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.annotation.*
import com.frankhon.fantasymusic.application.Fantasy
import com.frankhon.fantasymusic.media.AudioPlayerService
import com.hon.mylogger.MyLogger
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Frank Hon on 2022/11/23 5:52 下午.
 * E-mail: frank_hon@foxmail.com
 */

val packageId: String
    get() = Fantasy.appContext.packageName

val appContext = Fantasy.appContext

fun dp2px(dp: Int): Int {
    val density = Fantasy.appContext.resources.displayMetrics.density
    return (dp * density + 0.5f).toInt()
}

fun sp2px(dp: Int): Int {
    val density = Fantasy.appContext.resources.displayMetrics.scaledDensity
    return (dp * density + 0.5f).toInt()
}

inline fun <reified T> getSystemService(name: String): T {
    return Fantasy.appContext.getSystemService(name) as T
}

fun sendBroadcast(intent: Intent) {
    Fantasy.appContext.sendBroadcast(intent)
}

fun msToMMSS(millis: Long): String {
    try {
        val sdf = SimpleDateFormat("mm:ss", Locale.CHINA)
        return sdf.format(millis)
    } catch (e: IllegalArgumentException) {
        MyLogger.e(e)
    }
    return ""
}

fun formatTime(millis: Long): String {
    try {
        val sdf = SimpleDateFormat("MM-dd HH:mm:ss", Locale.CHINA)
        return sdf.format(millis)
    } catch (e: IllegalArgumentException) {
        MyLogger.e(e)
    }
    return ""
}

/**
 * @return 毫秒数 milli seconds
 */
fun transferLyricsTime(timeStr: String): Long {
    var tempList = timeStr.split(':')
    var sum = 0L
    if (tempList.size > 1) {
        val minutes = tempList[0].toInt()
        sum += minutes * 60 * 1000
        val rest = tempList[1]
        tempList = rest.split('.')
        if (tempList.size > 1) {
            val seconds = tempList[0].toInt()
            val milliSeconds = tempList[1].toInt()
            sum += seconds * 1000 + milliSeconds
            return sum
        }
    }
    return 0L
}

fun stopAudio() {
    startService(
        Intent(appContext, AudioPlayerService::class.java)
            .apply {
                action = ACTION_STOP
            }
    )
}

fun bindService(
    service: Intent,
    conn: ServiceConnection,
    flags: Int
): Boolean {
    return appContext.bindService(service, conn, flags)
}

fun startService(intent: Intent) {
    appContext.startService(intent)
}

fun string(@StringRes resId: Int) = Fantasy.appContext.getString(resId)

fun color(@ColorRes resId: Int) = Fantasy.appContext.color(resId)

fun drawable(@DrawableRes resId: Int) = Fantasy.appContext.drawable(resId)

fun getStringArray(@ArrayRes resId: Int): Array<String> =
    appContext.resources.getStringArray(resId)

fun getIntegerArray(@ArrayRes resId: Int): IntArray =
    appContext.resources.getIntArray(resId)

fun getQuantityString(@PluralsRes resId: Int, quantity: Int) =
    appContext.resources.getQuantityString(resId, quantity, quantity)