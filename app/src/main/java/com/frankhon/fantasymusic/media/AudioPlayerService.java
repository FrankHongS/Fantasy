package com.frankhon.fantasymusic.media;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import com.frankhon.fantasymusic.IMusicPlayer;
import com.frankhon.fantasymusic.activities.MainActivity;
import com.frankhon.fantasymusic.utils.Util;
import com.frankhon.fantasymusic.vo.SimpleSong;
import com.hon.mylogger.MyLogger;

import java.util.List;

/**
 * Created by Frank Hon on 2020/11/1 7:52 PM.
 * E-mail: frank_hon@foxmail.com
 */
public class AudioPlayerService extends Service {

    private final ServiceStub musicPlayer = new ServiceStub();

    @Override
    public void onCreate() {
        super.onCreate();
        MyLogger.d("onCreate");
        AudioPlayer.registerObserver(musicPlayer);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLogger.d("onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        MyLogger.d("onBind");
        return musicPlayer;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        AudioPlayer.unregisterObserver(musicPlayer);
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class ServiceStub extends IMusicPlayer.Stub implements AudioLifecycleObserver {

        private SimpleSong currentSong;
        private List<SimpleSong> playList;
        private int index = 0;

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void play(SimpleSong song) {
            if (song != null) {
                currentSong = song;
                startForeground(1,
                        Util.buildNotification(AudioPlayerService.this,
                                new Intent(AudioPlayerService.this, MainActivity.class),
                                song));
            }
            AudioPlayer.play(song.getLocation());
        }

        @Override
        public void setPlayList(List<SimpleSong> playList) {
            this.playList = playList;
            if (playList != null && !playList.isEmpty()) {
                play(playList.get(0));
                index = 0;
            }
        }

        @Override
        public void pause() {
            AudioPlayer.pause();
        }

        @Override
        public void resume() {
            AudioPlayer.resume();
        }

        //region Audio Lifecycle
        @Override
        public void onPrepare() {
        }

        @Override
        public void onPlaying() {
        }

        @Override
        public void onPause() {
        }

        @Override
        public void onResume() {
        }

        @Override
        public void onStop() {
        }

        @Override
        public void onCompleted() {
            if (playList != null && index + 1 < playList.size()) {
                play(playList.get(index + 1));
                index++;
            }
        }

        @Override
        public void onError() {
        }
        //endregion
    }
}
