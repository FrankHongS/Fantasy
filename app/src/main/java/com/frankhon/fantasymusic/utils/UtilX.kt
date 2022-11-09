package com.frankhon.fantasymusic.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.view.View
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.frankhon.fantasymusic.application.Fantasy
import com.frankhon.fantasymusic.media.AudioPlayerService
import com.hon.mylogger.MyLogger
import java.text.SimpleDateFormat
import java.util.*

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

fun String?.matchesUri(): Boolean {
    return this?.matches(Regex("^(https?://|file://).*")) ?: false
}

inline fun <reified T> getSystemService(name: String): T {
    return Fantasy.getAppContext().getSystemService(name) as T
}

val appContext = Fantasy.getAppContext() as Context

fun sendBroadcast(intent: Intent) {
    Fantasy.getAppContext().sendBroadcast(intent)
}

val packageId: String
    get() = Fantasy.getAppContext().packageName

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

fun <T> MutableList<T>.setData(data: List<T>) {
    clear()
    addAll(data)
}

fun bindService(
    service: Intent,
    conn: ServiceConnection,
    flags: Int
) {
    appContext.bindService(service, conn, flags)
}

fun startService(intent: Intent) {
    appContext.startService(intent)
}

fun Context.drawable(@DrawableRes resId: Int) = ContextCompat.getDrawable(this, resId)

fun Context.color(@ColorRes resId: Int) = ContextCompat.getColor(this, resId)

fun View.drawable(@DrawableRes resId: Int) = ContextCompat.getDrawable(context, resId)

fun View.color(@ColorRes resId: Int) = ContextCompat.getColor(context, resId)

fun getString(@StringRes resId: Int) = Fantasy.getAppContext().getString(resId)

fun getStringArray(@ArrayRes resId: Int): Array<String> =
    appContext.resources.getStringArray(resId)

fun getIntegerArray(@ArrayRes resId: Int): IntArray =
    appContext.resources.getIntArray(resId)

inline fun <reified T : Activity> Context.navigate() {
    startActivity(Intent(this, T::class.java))
}

fun stopAudio() {
    startService(
        Intent(appContext, AudioPlayerService::class.java)
            .apply {
                action = ACTION_STOP
            }
    )
}

/**
 * 获取当前栈顶的Fragment
 */
inline fun <reified T : Fragment> NavHostFragment.getSpecifiedFragment(): T? {
    return childFragmentManager.fragments.first() as? T
}
