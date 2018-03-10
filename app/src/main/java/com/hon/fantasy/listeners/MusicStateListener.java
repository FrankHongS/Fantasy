package com.hon.fantasy.listeners;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

/**
 * Listens for playback changes to send the the fragments bound to this activity
 */
public interface MusicStateListener {

    /**
     * Called when {@link com.hon.fantasy.MusicService#REFRESH} is invoked
     */
    void restartLoader();

    /**
     * Called when {@link com.hon.fantasy.MusicService#PLAYLIST_CHANGED} is invoked
     */
    void onPlaylistChanged();

    /**
     * Called when {@link com.hon.fantasy.MusicService#META_CHANGED} is invoked
     */
    void onMetaChanged();

}

