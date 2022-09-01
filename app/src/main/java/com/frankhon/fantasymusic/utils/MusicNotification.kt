@file:JvmName("Notification")

package com.frankhon.fantasymusic.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaMetadata
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.frankhon.fantasymusic.application.Fantasy
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.activities.MainActivity
import com.frankhon.fantasymusic.media.AudioPlayerService
import com.frankhon.fantasymusic.vo.CurrentPlayerInfo
import com.frankhon.fantasymusic.vo.SimpleSong

/**
 * Created by Frank Hon on 2022/8/16 6:36 下午.
 * E-mail: frank_hon@foxmail.com
 */

const val MUSIC_NOTIFICATION_ID = 1

fun sendMediaNotification(
    currentPlayerInfo: CurrentPlayerInfo,
    isPlaying: Boolean,
) {
    val song = currentPlayerInfo.curSong ?: return
    val context = Fantasy.getAppContext()
    Glide.with(context)
        .asBitmap()
        .load(song.songPic)
        .placeholder(R.drawable.song_pic)
        .into(object : CustomTarget<Bitmap?>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                val notificationManager = NotificationManagerCompat.from(context)
                val size = currentPlayerInfo.curPlayList.size
                val curIndex = currentPlayerInfo.curSongIndex
                val notification = buildNotification(
                    context,
                    song,
                    resource,
                    isPlaying,
                    curIndex != 0,
                    curIndex < size - 1
                )
                notificationManager.notify(MUSIC_NOTIFICATION_ID, notification)
            }

            override fun onLoadCleared(placeholder: Drawable?) {

            }
        })
}

fun sendMediaNotification(
    service: Service,
    currentPlayerInfo: CurrentPlayerInfo,
    isPlaying: Boolean
) {
    val song = currentPlayerInfo.curSong ?: return
    val context = Fantasy.getAppContext()
    Glide.with(context)
        .asBitmap()
        .load(song.songPic)
        .placeholder(R.drawable.song_pic)
        .into(object : CustomTarget<Bitmap?>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                val size = currentPlayerInfo.curPlayList.size
                val curIndex = currentPlayerInfo.curSongIndex
                val notification = buildNotification(
                    context,
                    song,
                    resource,
                    isPlaying,
                    curIndex != 0,
                    curIndex < size - 1
                )
                service.startForeground(MUSIC_NOTIFICATION_ID, notification)
            }

            override fun onLoadCleared(placeholder: Drawable?) {

            }
        })
}

private fun buildNotification(
    context: Context,
    song: SimpleSong,
    resource: Bitmap,
    isPlaying: Boolean,
    isPreviousEnable: Boolean,
    isNextEnable: Boolean
): Notification {
    // build a mediaSession
    val mediaSession = MediaSessionCompat(context, "MediaNotification").apply {
        setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, song.name)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, song.artist)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, song.duration)
                .build()
        )
    }
    val contentIntent = PendingIntent.getActivity(
        context,
        1,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    return NotificationCompat.Builder(context, PLAYER_CHANNEL_ID)
        .setContentIntent(contentIntent)
        .setSmallIcon(R.drawable.ic_baseline_music_note_24)
        .setLargeIcon(resource)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        .addAction(
            R.drawable.ic_previous_song_notification, "Previous",
            if (isPreviousEnable) context.getButtonPendingIntent(ACTION_PREVIOUS) else null
        )
        .addAction {
            if (isPlaying) {
                addAction(
                    R.drawable.ic_pause_song_notification, "Pause",
                    context.getButtonPendingIntent(ACTION_PAUSE)
                )
            } else {
                addAction(
                    R.drawable.ic_play_song_notification, "Play",
                    context.getButtonPendingIntent(ACTION_RESUME)
                )
            }
        }
        .addAction(
            R.drawable.ic_next_song_notification, "Next",
            if (isNextEnable) context.getButtonPendingIntent(ACTION_NEXT) else null
        )
        .addAction(
            R.drawable.ic_stop_song_notification, "Stop",
//                            MediaButtonReceiver.buildMediaButtonPendingIntent(
//                                context,
//                                PlaybackStateCompat.ACTION_STOP
//                            )
            context.getButtonPendingIntent(ACTION_STOP)
        )
        .addAction(R.drawable.ic_favorite_song_notification, "Favorite", null)
        .setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
                .setMediaSession(mediaSession.sessionToken)
        )
        .setOngoing(true)
        .setAutoCancel(false)
        .build()
}

fun cancelNotification() {
    val notificationManager = NotificationManagerCompat.from(Fantasy.getAppContext())
    notificationManager.cancel(MUSIC_NOTIFICATION_ID)
}

fun createNotificationChannel() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            PLAYER_CHANNEL_ID,
            PLAYER_CHANNEL_ID,
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager =
            getSystemService<NotificationManager>(Context.NOTIFICATION_SERVICE)
        notificationManager.createNotificationChannel(channel)
    }
}

private fun NotificationCompat.Builder.addAction(delegate: (NotificationCompat.Builder.() -> Unit)): NotificationCompat.Builder {
    delegate()
    return this
}

private fun Context.getButtonPendingIntent(action: String): PendingIntent {
    return PendingIntent.getService(
        this,
        0,
        Intent(this, AudioPlayerService::class.java)
            .apply {
                setAction(action)
            },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

/**
 * Note: Starting with Android 12 (API level 31), apps targeting Android 12 or newer are no longer able to create fully custom notifications.
 */
fun buildNormalMediaNotification(context: Context) {
    val collapsedView = RemoteViews(context.packageName, R.layout.layout_collapsed_notification)
    val expandedView = RemoteViews(context.packageName, R.layout.layout_expanded_notification)

    val notification = NotificationCompat.Builder(context, PLAYER_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_baseline_music_note_24)
        .setTicker("开始播放啦")
        .setOngoing(true)
        .setCustomContentView(collapsedView)
        .setCustomBigContentView(expandedView)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .build()
    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.notify(1, notification)
}