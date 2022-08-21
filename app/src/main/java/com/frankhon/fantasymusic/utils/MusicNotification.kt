@file:JvmName("Notification")

package com.frankhon.fantasymusic.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaMetadata
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.session.MediaButtonReceiver
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.media.AudioPlayerService
import com.frankhon.fantasymusic.vo.SimpleSong

/**
 * Created by Frank Hon on 2022/8/16 6:36 下午.
 * E-mail: frank_hon@foxmail.com
 */

private const val MUSIC_NOTIFICATION_ID = 1

fun buildMediaNotification(context: Context, song: SimpleSong, isPlaying: Boolean) {
    Glide.with(context)
        .asBitmap()
        .load(song.songPic)
        .placeholder(R.drawable.song_pic)
        .into(object : CustomTarget<Bitmap?>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
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
                val notificationManager = NotificationManagerCompat.from(context)
                val notification =
                    NotificationCompat.Builder(context, PLAYER_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                        .setLargeIcon(resource)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                        .addAction(
                            R.drawable.ic_previous_song_notification, "Previous",
                            context.getPendingIntent(ACTION_PREVIOUS)
                        )
                        .addAction {
                            if (isPlaying) {
                                addAction(
                                    R.drawable.ic_pause_song_notification, "Pause",
                                    context.getPendingIntent(ACTION_PAUSE)
                                )
                            } else {
                                addAction(
                                    R.drawable.ic_play_song_notification, "Play",
                                    context.getPendingIntent(ACTION_RESUME)
                                )
                            }
                        }
                        .addAction(
                            R.drawable.ic_next_song_notification, "Next",
                            context.getPendingIntent(ACTION_NEXT)
                        )
                        .addAction(
                            R.drawable.ic_stop_song_notification, "Stop",
//                            MediaButtonReceiver.buildMediaButtonPendingIntent(
//                                context,
//                                PlaybackStateCompat.ACTION_STOP
//                            )
                            context.getPendingIntent(ACTION_STOP)
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
                notificationManager.notify(MUSIC_NOTIFICATION_ID, notification)
            }

            override fun onLoadCleared(placeholder: Drawable?) {

            }
        })
}

fun cancelNotification(context: Context) {
    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.cancel(MUSIC_NOTIFICATION_ID)
}

private fun NotificationCompat.Builder.addAction(delegate: (NotificationCompat.Builder.() -> Unit)): NotificationCompat.Builder {
    delegate()
    return this
}

private fun Context.getPendingIntent(action: String): PendingIntent {
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