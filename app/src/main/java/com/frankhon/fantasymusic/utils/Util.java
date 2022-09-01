package com.frankhon.fantasymusic.utils;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.session.MediaSession;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.frankhon.fantasymusic.application.Fantasy;
import com.frankhon.fantasymusic.R;
import com.frankhon.fantasymusic.receivers.MusicInfoReceiver;
import com.frankhon.fantasymusic.vo.SimpleSong;

/**
 * Created by Frank_Hon on 1/6/2020.
 * E-mail: v-shhong@microsoft.com
 */
public final class Util {

    public static int dp2px(int dp) {
        float density = Fantasy.getAppContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId,
                channelName, importance);
        NotificationManager notificationManager =
                (NotificationManager) Fantasy.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    public static Notification buildNotification(Context context, Intent intent, SimpleSong song) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, FLAG_IMMUTABLE);

        Intent intentPrevious = new Intent(context, MusicInfoReceiver.class)
                .setAction(Constants.ACTION_PREVIOUS);
        PendingIntent previousPendingIntent = PendingIntent.getBroadcast(context, 0,
                intentPrevious, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
        Intent intentPlay = new Intent(context, MusicInfoReceiver.class)
                .setAction(Constants.ACTION_PREVIOUS);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(context, 0,
                intentPlay, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
        Intent intentNext = new Intent(context, MusicInfoReceiver.class)
                .setAction(Constants.ACTION_PREVIOUS);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 0,
                intentNext, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);

        Notification.Style mediaStyle = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MediaSession mediaSession = new MediaSession(context, "PlayerService");
            mediaStyle = new Notification.MediaStyle()
                    .setMediaSession(mediaSession.getSessionToken());
        }

        Notification notification = new NotificationCompat.Builder(context, Constants.PLAYER_CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setContentTitle(song.getName())
                .setContentText(song.getArtist())
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.song_pic))
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .addAction(R.drawable.ic_previous_song_notification, "Previous", previousPendingIntent)
                .addAction(R.drawable.ic_play_song_notification, "Play", playPendingIntent)
                .addAction(R.drawable.ic_next_song_notification, "Next", nextPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        return notification;
    }
}
