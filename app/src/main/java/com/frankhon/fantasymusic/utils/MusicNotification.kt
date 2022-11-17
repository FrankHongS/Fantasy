@file:JvmName("Notification")

package com.frankhon.fantasymusic.utils

import android.annotation.SuppressLint
import android.app.*
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.application.Fantasy
import com.frankhon.fantasymusic.media.AudioPlayerService
import com.frankhon.fantasymusic.media.PlayMode
import com.frankhon.fantasymusic.media.isPlayingInNotification
import com.frankhon.fantasymusic.ui.activities.MainActivity
import com.frankhon.fantasymusic.vo.CurrentPlayerInfo
import com.frankhon.fantasymusic.vo.SimpleSong

/**
 * Created by Frank Hon on 2022/8/16 6:36 下午.
 * E-mail: frank_hon@foxmail.com
 */

private const val MUSIC_NOTIFICATION_ID = 1

fun sendMediaNotification(
    isSystemStyle: Boolean,
    currentPlayerInfo: CurrentPlayerInfo,
) {
    val song = currentPlayerInfo.curSong ?: return
    val context = Fantasy.getAppContext()
    Glide.with(context)
        .asBitmap()
        .load(song.picUrl)
        .placeholder(R.drawable.song_pic)
        .into(object : CustomTarget<Bitmap?>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                currentPlayerInfo.run {
                    val isPlaying = curPlayerState.isPlayingInNotification()
                    val notification = if (isSystemStyle) {
                        buildNotification(
                            context,
                            song,
                            resource,
                            isPlaying,
                            if (curPlayMode == PlayMode.LOOP_SINGLE) curSongIndex != 0 else true,
                            if (curPlayMode == PlayMode.LOOP_SINGLE)
                                curSongIndex < curPlaylist.size - 1 else true,
                            curPlaybackPosition
                        )
                    } else {
                        buildNormalMediaNotification(context, song, resource, isPlaying)
                    }
                    val notificationManager = NotificationManagerCompat.from(context)
                    notificationManager.notify(MUSIC_NOTIFICATION_ID, notification)
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) {

            }
        })
}

fun sendMediaNotification(
    isSystemStyle: Boolean,
    service: Service,
    currentPlayerInfo: CurrentPlayerInfo
) {
    val song = currentPlayerInfo.curSong ?: return
    val context = Fantasy.getAppContext()
    Glide.with(context)
        .asBitmap()
        .load(song.picUrl)
        .placeholder(R.drawable.song_pic)
        .into(object : CustomTarget<Bitmap?>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                currentPlayerInfo.run {
                    val isPlaying = curPlayerState.isPlayingInNotification()
                    val notification = if (isSystemStyle) {
                        buildNotification(
                            context,
                            song,
                            resource,
                            isPlaying,
                            if (curPlayMode == PlayMode.LOOP_SINGLE) curSongIndex != 0 else true,
                            if (curPlayMode == PlayMode.LOOP_SINGLE)
                                curSongIndex < curPlaylist.size - 1 else true,
                            curPlaybackPosition
                        )
                    } else {
                        buildNormalMediaNotification(context, song, resource, isPlaying)
                    }
                    service.startForeground(MUSIC_NOTIFICATION_ID, notification)
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) {

            }
        })
}

/**
 * There is an internal Android MediaSessions limit SESSION_CREATION_LIMIT_PER_UID = 100;
 * You should release MediaSession instances that you don't need anymore.
 *
 * Exception:
 * java.lang.NullPointerException: Attempt to invoke interface method
 * 'android.media.session.ISessionController android.media.session.ISession.getController()'
 */
private var lastMediaSession: MediaSessionCompat? = null

@SuppressLint("InlinedApi")
private fun buildNotification(
    context: Context,
    song: SimpleSong,
    resource: Bitmap,
    isPlaying: Boolean,
    isPreviousEnable: Boolean,
    isNextEnable: Boolean,
    curPlaybackPosition: Long
): Notification {
    // build a mediaSession
    lastMediaSession?.release()
    val mediaSession = MediaSessionCompat(context, "MediaNotification").apply {
        setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, song.name)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, song.artist)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, song.duration)
                .build()
        )
        setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                    curPlaybackPosition,
                    1f
                )
                .build()
        )
        lastMediaSession = this
    }
    val contentIntent = PendingIntent.getActivity(
        context,
        1,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    return NotificationCompat.Builder(context, PLAYER_CHANNEL_ID)
        .setContentIntent(contentIntent)
        .setSmallIcon(R.drawable.ic_notification_icon)
        .setLargeIcon(resource)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        .addAction(
            R.drawable.ic_previous_song_notification_large, "Previous",
            if (isPreviousEnable) context.getButtonPendingIntent(ACTION_PREVIOUS) else null
        )
        .addAction {
            if (isPlaying) {
                addAction(
                    R.drawable.ic_pause_song_notification_large, "Pause",
                    context.getButtonPendingIntent(ACTION_PAUSE)
                )
            } else {
                addAction(
                    R.drawable.ic_play_song_notification_large, "Play",
                    context.getButtonPendingIntent(ACTION_RESUME)
                )
            }
        }
        .addAction(
            R.drawable.ic_next_song_notification_large, "Next",
            if (isNextEnable) context.getButtonPendingIntent(ACTION_NEXT) else null
        )
        .addAction(
            R.drawable.ic_stop_song_notification_large, "Stop",
//            MediaButtonReceiver.buildMediaButtonPendingIntent(
//                context,
//                PlaybackStateCompat.ACTION_STOP
//            )
            context.getButtonPendingIntent(ACTION_STOP)
        )
        .addAction(R.drawable.ic_favorite_song_notification_large, "Favorite", null)
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
    releaseMediaSession()
}

fun releaseMediaSession() {
    lastMediaSession?.release()
    lastMediaSession = null
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

private fun NotificationCompat.Builder.addAction(delegate: (NotificationCompat.Builder.() -> Unit))
        : NotificationCompat.Builder {
    delegate()
    return this
}

/**
 * Note: Starting with Android 12 (API level 31), apps targeting Android 12 or newer are no longer able to create fully custom notifications.
 */
@SuppressLint("InlinedApi")
fun buildNormalMediaNotification(
    context: Context,
    song: SimpleSong,
    albumCover: Bitmap,
    isPlaying: Boolean,
): Notification {
    val collapsedView = RemoteViews(context.packageName, R.layout.layout_collapsed_notification)
    val expandedView = RemoteViews(context.packageName, R.layout.layout_expanded_notification)

    collapsedView.run {
        setImageViewBitmap(R.id.iv_notification_song_cover, albumCover)
        setImageViewResource(
            R.id.iv_notification_toggle_song,
            if (isPlaying) R.drawable.ic_pause_song_notification
            else R.drawable.ic_play_song_notification
        )
        setOnClickPendingIntent(
            R.id.iv_notification_toggle_song,
            context.getButtonPendingIntent(
                if (isPlaying) {
                    ACTION_PAUSE
                } else {
                    ACTION_RESUME
                }
            )
        )
        setOnClickPendingIntent(
            R.id.iv_notification_next_song,
            context.getButtonPendingIntent(ACTION_NEXT)
        )
        setOnClickPendingIntent(
            R.id.iv_notification_stop_song,
            context.getButtonPendingIntent(ACTION_STOP)
        )
    }

    expandedView.run {
        setTextViewText(R.id.tv_notification_song_name, song.name)
        setTextViewText(R.id.tv_notification_artist_name, song.artist)
        setImageViewBitmap(R.id.iv_notification_song_cover, albumCover)
        setImageViewResource(
            R.id.iv_notification_toggle_song,
            if (isPlaying) R.drawable.ic_pause_song_notification
            else R.drawable.ic_play_song_notification
        )
        setOnClickPendingIntent(
            R.id.iv_notification_toggle_song,
            context.getButtonPendingIntent(
                if (isPlaying) {
                    ACTION_PAUSE
                } else {
                    ACTION_RESUME
                }
            )
        )
        setOnClickPendingIntent(
            R.id.iv_notification_prev_song,
            context.getButtonPendingIntent(ACTION_PREVIOUS)
        )
        setOnClickPendingIntent(
            R.id.iv_notification_next_song,
            context.getButtonPendingIntent(ACTION_NEXT)
        )
        setOnClickPendingIntent(
            R.id.iv_notification_stop_song,
            context.getButtonPendingIntent(ACTION_STOP)
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
        .setSmallIcon(R.drawable.ic_notification_icon)
        .setOngoing(true)
        .setCustomContentView(collapsedView)
        .setCustomBigContentView(expandedView)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .build()
}

@SuppressLint("InlinedApi")
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