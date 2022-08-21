package com.frankhon.fantasymusic.media;

import static com.frankhon.fantasymusic.utils.Constants.ACTION_NEXT;
import static com.frankhon.fantasymusic.utils.Constants.ACTION_PAUSE;
import static com.frankhon.fantasymusic.utils.Constants.ACTION_PREVIOUS;
import static com.frankhon.fantasymusic.utils.Constants.ACTION_RESUME;
import static com.frankhon.fantasymusic.utils.Constants.ACTION_STOP;
import static com.frankhon.fantasymusic.utils.Constants.KEY_CUR_SONG;
import static com.frankhon.fantasymusic.utils.Constants.KEY_DURATION;
import static com.frankhon.fantasymusic.utils.Constants.KEY_PLAYER_STATE;
import static com.frankhon.fantasymusic.utils.Constants.KEY_SONG_PROGRESS;
import static com.frankhon.fantasymusic.utils.Constants.MUSIC_INFO_ACTION;
import static com.frankhon.fantasymusic.utils.Constants.MUSIC_PROGRESS_ACTION;
import static com.frankhon.fantasymusic.utils.Constants.PACKAGE_ID;
import static com.frankhon.fantasymusic.utils.Notification.buildMediaNotification;
import static com.frankhon.fantasymusic.utils.Notification.cancelNotification;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.session.MediaButtonReceiver;

import com.frankhon.fantasymusic.Fantasy;
import com.frankhon.fantasymusic.IMusicPlayer;
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
        AudioPlayer.registerObserver(musicPlayer);
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
        MyLogger.d("onStartCommand");
//        MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
        String action = intent.getAction();
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
                cancelNotification(this);
                break;
            default:
                break;
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
        AudioPlayer.release();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class ServiceStub extends IMusicPlayer.Stub implements AudioLifecycleObserver {

        private SimpleSong curSong;
        private PlayerState curState = PlayerState.IDLE;

        @Override
        public void play(SimpleSong song) {
            AudioPlayer.play(song);
        }

        @Override
        public void setPlayList(List<SimpleSong> playList, int index) {
            AudioPlayer.setPlayList(playList, index);
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

        //region Audio Lifecycle
        @Override
        public void onPrepare(@NonNull SimpleSong song) {
            this.curSong = song;
            this.curState = PlayerState.PREPARING;
            sendState();
        }

        @Override
        public void onPlaying(@NonNull SimpleSong song) {
            this.curSong = song;
            this.curState = PlayerState.PLAYING;
//            startForeground(1,
//                    Util.buildNotification(AudioPlayerService.this,
//                            new Intent(AudioPlayerService.this, MainActivity.class),
//                            curSong));
//            Notification.buildNormalMediaNotification(AudioPlayerService.this);
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
            AudioPlayer.next();
        }

        @Override
        public void onFinished() {
            this.curState = PlayerState.FINISHED;
            sendState();
        }

        @Override
        public void onError() {
        }

        @Override
        public void onProgressUpdated(int curPosition, int duration) {
            updateProgress(curPosition, duration);
        }

        //endregion

        private void sendState() {
            if (curSong != null) {
                buildMediaNotification(AudioPlayerService.this, curSong, curState == PlayerState.PLAYING
                        || curState == PlayerState.PREPARING);

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
            intent.setPackage(Fantasy.getAppContext().getPackageName());
            intent.putExtra(KEY_SONG_PROGRESS, curPosition);
            intent.putExtra(KEY_DURATION, duration);
            Fantasy.getAppContext().sendBroadcast(intent);
        }
    }
}
