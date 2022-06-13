// IMusicPlayer.aidl
package com.frankhon.fantasymusic;

// Declare any non-default types here with import statements
import com.frankhon.fantasymusic.vo.SimpleSong;

interface IMusicPlayer {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    void play(in SimpleSong song);
    void pause();
    void resume();
}
