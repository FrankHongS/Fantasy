package com.hon.fantasy.dataloader;

import android.content.Context;

import com.hon.fantasy.models.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frank on 2018/3/4.
 * E-mail:frank_hon@foxmail.com
 */

public class QueueLoader {


    private static NowPlayingCursor mCursor;

    public static List<Song> getQueueSongs(Context context) {

        final ArrayList<Song> mSongList = new ArrayList<>();
        mCursor = new NowPlayingCursor(context);

        if (mCursor != null && mCursor.moveToFirst()) {
            do {

                final long id = mCursor.getLong(0);

                final String songName = mCursor.getString(1);

                final String artist = mCursor.getString(2);

                final long albumId = mCursor.getLong(3);

                final String album = mCursor.getString(4);

                final int duration = mCursor.getInt(5);

                final long artistid = mCursor.getInt(7);

                final int tracknumber = mCursor.getInt(6);

                final Song song = new Song(id, albumId, artistid, songName, artist, album, duration, tracknumber);

                mSongList.add(song);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mSongList;
    }
}
