package com.frankhon.fantasymusic.media;

import static com.frankhon.fantasymusic.utils.Constants.ACTION_NEXT;
import static com.frankhon.fantasymusic.utils.Constants.ACTION_PAUSE;
import static com.frankhon.fantasymusic.utils.Constants.ACTION_PREVIOUS;
import static com.frankhon.fantasymusic.utils.Constants.ACTION_RESUME;
import static com.frankhon.fantasymusic.utils.Constants.ACTION_STOP;
import static com.frankhon.fantasymusic.utils.Notification.sendMediaNotification;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.frankhon.fantasymusic.IMusicPlayer;
import com.frankhon.fantasymusic.vo.CurrentPlayerInfo;
import com.frankhon.fantasymusic.vo.SimpleSong;
import com.hon.mylogger.MyLogger;

import java.util.List;

/**
 * Created by Frank Hon on 2020/11/1 7:52 PM.
 * E-mail: frank_hon@foxmail.com
 */
public class AudioPlayerService extends Service {

    private final ServiceStub musicPlayer = new ServiceStub();
//    private MediaSessionCompat mediaSessionCompat;

    @Override
    public void onCreate() {
        super.onCreate();
        MyLogger.d("onCreate");
//        mediaSessionCompat = new MediaSessionCompat(this, "MediaService");
//        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
//            @Override
//            public void onStop() {
//                stopForeground(true);
//            }
//        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
        String action = intent.getAction();
        MyLogger.d("onStartCommand: " + action);
        if (action != null) {
            switch (action) {
                case ACTION_PREVIOUS:
                    AudioPlayer.previous();
                    break;
                case ACTION_NEXT:
                    AudioPlayer.next();
                    break;
                case ACTION_RESUME:
                    AudioPlayer.resume();
                    break;
                case ACTION_PAUSE:
                    AudioPlayer.pause();
                    break;
                case ACTION_STOP:
                    stopForeground(false);
                    AudioPlayer.release();
                    break;
                default:
                    break;
            }
        }
        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        MyLogger.d("onBind");
        return musicPlayer;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        MyLogger.d("onUnbind");
//        AudioPlayer.release();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        MyLogger.d("onDestroy");
        super.onDestroy();
    }

    private class ServiceStub extends IMusicPlayer.Stub {

        @Override
        public void play(SimpleSong song) {
            AudioPlayer.play(song);
        }

        @Override
        public void playAndAddIntoPlaylist(SimpleSong song) {
            AudioPlayer.playAndAddIntoPlaylist(song);
            sendMediaNotification(AudioPlayerService.this, AudioPlayer.getCurrentPlayerInfo(), true);
        }

        @Override
        public void addIntoPlaylist(SimpleSong song) {
            boolean isPlaying = AudioPlayer.addIntoPlaylist(song);
            if (isPlaying) {
                sendMediaNotification(AudioPlayerService.this, AudioPlayer.getCurrentPlayerInfo(), true);
            }
        }

        @Override
        public void removeSongFromPlayList(int index) {
            AudioPlayer.removeSongFromPlayList(index);
        }

        @Override
        public void setPlayList(List<SimpleSong> playList, int index) {
            AudioPlayer.setPlaylist(playList, index);
            sendMediaNotification(AudioPlayerService.this, AudioPlayer.getCurrentPlayerInfo(), true);
        }

        @Override
        public void pause() {
            AudioPlayer.pause();
        }

        @Override
        public void resume() {
            AudioPlayer.resume();
        }

        @Override
        public void next() {
            AudioPlayer.next();
        }

        @Override
        public void previous() {
            AudioPlayer.previous();
        }

        @Override
        public void seekTo(int msec) {
            AudioPlayer.seekTo(msec);
        }

        @Override
        public void setPlayMode(String playMode) {
            AudioPlayer.setPlayMode(playMode);
        }

        @Override
        public CurrentPlayerInfo getCurrentPlayerInfo() {
            return AudioPlayer.getCurrentPlayerInfo();
        }
    }
}
