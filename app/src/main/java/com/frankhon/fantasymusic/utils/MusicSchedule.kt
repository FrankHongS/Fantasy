package com.frankhon.fantasymusic.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.frankhon.fantasymusic.R
import com.hon.mylogger.MyLogger

/**
 * Created by Frank Hon on 2022/11/5 11:29 上午.
 * E-mail: frank_hon@foxmail.com
 */

@SuppressLint("InlinedApi")
fun schedulePause(minutes: Int) {
    MyLogger.d("schedulePause: ${formatTime(System.currentTimeMillis())}")
    //删除之前的定时
    removeSchedulePause()
    val intent = Intent(ACTION_SCHEDULE_PAUSE_MUSIC)
    val pendingIntent = PendingIntent.getBroadcast(
        appContext,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val pauseAtTime = System.currentTimeMillis() + minutes * 60 * 1000
    val alarmManager = getSystemService<AlarmManager>(Context.ALARM_SERVICE)
    alarmManager.set(AlarmManager.RTC_WAKEUP, pauseAtTime, pendingIntent)
}

@SuppressLint("InlinedApi")
fun removeSchedulePause() {
    val alarmManager = getSystemService<AlarmManager>(Context.ALARM_SERVICE)
    val intent = Intent(ACTION_SCHEDULE_PAUSE_MUSIC)
    val pendingIntent = PendingIntent.getBroadcast(
        appContext,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
}