package com.frankhon.fantasymusic.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.frankhon.fantasymusic.Fantasy;
import com.frankhon.fantasymusic.IMusicPlayer;

/**
 * Created by Frank Hon on 2020/11/1 8:26 PM.
 * E-mail: frank_hon@foxmail.com
 */
public final class MusicPlayer {

    private static volatile MusicPlayer INSTANCE;

    private IMusicPlayer musicPlayer;

    private MusicPlayer() {
        Intent intent = new Intent(Fantasy.getAppContext(), MusicPlayerService.class);
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                musicPlayer = (IMusicPlayer) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        Fantasy.getAppContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public static MusicPlayer getInstance() {
        if (INSTANCE == null) {
            synchronized (MusicPlayer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MusicPlayer();
                }
            }
        }
        return INSTANCE;
    }

    public void play(String audioFile){
        try {
            musicPlayer.play(audioFile);
        } catch (RemoteException e) {
            // do nothing
        }
    }

    public void pause(){
        try {
            musicPlayer.pause();
        } catch (RemoteException e) {
            // do nothing
        }
    }

    public void resume(){
        try {
            musicPlayer.resume();
        } catch (RemoteException e) {
            // do nothing
        }
    }
}
