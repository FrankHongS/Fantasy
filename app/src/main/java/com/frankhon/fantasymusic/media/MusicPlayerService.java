package com.frankhon.fantasymusic.media;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import com.frankhon.fantasymusic.IMusicPlayer;
import com.frankhon.fantasymusic.vo.PlaySongEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Frank Hon on 2020/11/1 7:52 PM.
 * E-mail: frank_hon@foxmail.com
 */
public class MusicPlayerService extends Service {

    private IMusicPlayer.Stub musicPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        musicPlayer = new ServiceStub();
        return musicPlayer;
    }

    private static class ServiceStub extends IMusicPlayer.Stub {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void play(String audioFilePath) {
            MediaPlayerManager.getInstance().play(audioFilePath,
                    mp -> EventBus.getDefault().post(new PlaySongEvent(false)));
        }

        @Override
        public void pause() {
            MediaPlayerManager.getInstance().pause();
        }

        @Override
        public void resume() {
            MediaPlayerManager.getInstance().resume();
        }
    }
}
