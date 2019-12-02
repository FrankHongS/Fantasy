package com.hon.fantasy;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.hon.fantasy.utils.NotificationUtil;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Created by Frank_Hon on 12/2/2019.
 * E-mail: v-shhong@microsoft.com
 */
public class MusicServiceTemp extends Service {

    private static final String HANDLER_THREAD_NAME = "MusicPlayerHandlerThread";

    private HandlerThread mHandlerThread;

    private MediaPlayer mCurrentMediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationUtil.createNotificationChannel();

        initHandler();

        initPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initHandler() {
        mHandlerThread = new HandlerThread(HANDLER_THREAD_NAME, THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
    }

    private void initPlayer() {
        mCurrentMediaPlayer = new MediaPlayer();
    }
}
