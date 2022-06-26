package com.frankhon.fantasymusic.media;

import static com.frankhon.fantasymusic.utils.Constants.KEY_CUR_SONG;
import static com.frankhon.fantasymusic.utils.Constants.KEY_DURATION;
import static com.frankhon.fantasymusic.utils.Constants.KEY_PLAYER_STATE;
import static com.frankhon.fantasymusic.utils.Constants.KEY_SONG_PROGRESS;
import static com.frankhon.fantasymusic.utils.Constants.MUSIC_INFO_ACTION;
import static com.frankhon.fantasymusic.utils.Constants.MUSIC_PROGRESS_ACTION;
import static com.frankhon.fantasymusic.utils.Constants.PACKAGE_ID;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.frankhon.fantasymusic.Fantasy;
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

        private SimpleSong curSong;
        private List<SimpleSong> playList;
        private int curIndex = 0;
        private PlayerState curState = PlayerState.IDLE;

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void play(SimpleSong song) {
            if (song != null) {
                startForeground(1,
                        Util.buildNotification(AudioPlayerService.this,
                                new Intent(AudioPlayerService.this, MainActivity.class),
                                song));
                AudioPlayer.play(song);
            }
        }

        @Override
        public void setPlayList(List<SimpleSong> playList, int index) {
            this.playList = playList;
            play(index);
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
            int index = curIndex + 1;
            play(index);
        }

        @Override
        public void previous() {
            int index = curIndex - 1;
            play(index);
        }

        @Override
        public void seekTo(int msec) {
            AudioPlayer.seekTo(msec);
        }

        //region Audio Lifecycle
        @Override
        public void onPrepare(@NonNull SimpleSong song) {
            this.curSong = song;
            this.curState = PlayerState.PREPARING;
            sendState();
        }

        @Override
        public void onPlaying() {
            this.curState = PlayerState.PLAYING;
            sendState();
        }

        @Override
        public void onPause() {
            this.curState = PlayerState.PAUSED;
            sendState();
        }

        @Override
        public void onResume() {
            this.curState = PlayerState.RESUMED;
            sendState();
        }

        @Override
        public void onStop() {

        }

        @Override
        public void onCompleted() {
            this.curState = PlayerState.COMPLETED;
            sendState();
            int index = curIndex + 1;
            play(index);
        }

        @Override
        public void onError() {
        }

        @Override
        public void onProgressUpdated(int curPosition, int duration) {
            updateProgress(curPosition, duration);
        }

        //endregion

        private void play(int index) {
            if (playList != null && index >= 0 && index < playList.size()) {
                SimpleSong song = playList.get(index);
                AudioPlayer.play(song);
                curIndex = index;
            }
        }

        private void sendState() {
            if (curSong != null) {
                Intent intent = new Intent(MUSIC_INFO_ACTION);
                //在Android 8.0 以上要求静态注册的BroadcastReceiver所接收的消息必须是显式的，
                // 我们通过设置包名的方式来告诉系统这个Intent是要发给哪个应用来接收。不设置的话就会接收不到消息
                intent.setPackage(PACKAGE_ID);
                intent.putExtra(KEY_PLAYER_STATE, curState.name());
                intent.putExtra(KEY_CUR_SONG, curSong);
                Fantasy.getAppContext().sendBroadcast(intent);
            }
        }

        private void updateProgress(int curPosition, int duration) {
            Intent intent = new Intent(MUSIC_PROGRESS_ACTION);
            intent.setPackage(PACKAGE_ID);
            intent.putExtra(KEY_SONG_PROGRESS, curPosition);
            intent.putExtra(KEY_DURATION, duration);
            Fantasy.getAppContext().sendBroadcast(intent);
        }
    }
}
