package com.hon.fantasy.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;

import com.hon.fantasy.Fantasy;
import com.hon.fantasy.R;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by Frank_Hon on 11/29/2019.
 * E-mail: v-shhong@microsoft.com
 */
public final class NotificationUtil {

    private NotificationUtil() {
    }

    public static void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationManager manager = (NotificationManager) Fantasy.sContext.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = null;
            channel = new NotificationChannel(Constants.CHANNEL_ID, "Fantasy", importance);
            manager.createNotificationChannel(channel);
        }
    }

    public static Notification buildNotification(String album, String artist, boolean isPlaying, long albumId,
                                                 long postTime, String trackName,
                                                 PendingIntent previous, PendingIntent pause, PendingIntent next,
                                                 MediaSessionCompat.Token token) {
        String text = TextUtils.isEmpty(album) ? artist : artist + " - " + album;

        int playButtonResId = isPlaying ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_white_36dp;

        Intent nowPlayingIntent = NavigationUtils.getNowPlayingIntent();
        PendingIntent clickIntent = PendingIntent.getActivity(Fantasy.sContext, 0, nowPlayingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap artwork = ImageLoader.getInstance().loadImageSync(FantasyUtils.getAlbumArtUri(albumId).toString());
        if (artwork == null) {
            artwork = ImageLoader.getInstance().loadImageSync("drawable://" + R.drawable.ic_empty_music2);
        }

        Builder builder = new Builder(Fantasy.sContext, Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(artwork)
                .setContentIntent(clickIntent)
                .setContentTitle(trackName)
                .setContentText(text)
                .setWhen(postTime)
                .addAction(R.drawable.ic_skip_previous_white_36dp, "", previous)
                .addAction(playButtonResId, "", pause)
                .addAction(R.drawable.ic_skip_next_white_36dp, "", next);

        if (FantasyUtils.isJellyBeanMR1()) {
            builder.setShowWhen(false);
        }

        if (FantasyUtils.isLollipop()) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            MediaStyle style = new MediaStyle()
                    .setMediaSession(token)
                    .setShowActionsInCompactView(0, 1, 2, 3);
            builder.setStyle(style);
        }
        if (artwork != null && FantasyUtils.isLollipop()) {
            builder.setColor(Palette.from(artwork).generate().getVibrantColor(Color.parseColor("#403f4d")));
        }

        if (FantasyUtils.isOreo()) {
            builder.setColorized(true);
        }

        return builder.build();
    }

    public static void cancelNotification(int id) {
        NotificationManager manager = (NotificationManager) Fantasy.sContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(id);
    }

    public static void notifyNotification(int id, Notification notification) {
        NotificationManager manager = (NotificationManager) Fantasy.sContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(id, notification);
    }

}
