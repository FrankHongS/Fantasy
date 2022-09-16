// IMusicPlayer.aidl
package com.frankhon.fantasymusic;

// Declare any non-default types here with import statements
import com.frankhon.fantasymusic.vo.SimpleSong;
import com.frankhon.fantasymusic.vo.CurrentPlayerInfo;
import java.util.List;

interface IMusicPlayer {

    void play(in SimpleSong song);
    void playAndAddIntoPlaylist(in SimpleSong song);
    void addIntoPlaylist(in SimpleSong song);
    void setPlayList(in List<SimpleSong> songs, int index);
    void removeSongFromPlayList(int index);
    void pause();
    void resume();
    void next();
    void previous();
    void seekTo(int progress);
    void setPlayMode(String playMode);
    CurrentPlayerInfo getCurrentPlayerInfo();
}
