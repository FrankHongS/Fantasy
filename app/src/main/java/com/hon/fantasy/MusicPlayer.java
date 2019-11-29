package com.hon.fantasy;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.widget.Toast;

import com.hon.fantasy.dataloader.SongLoader;
import com.hon.fantasy.helpers.MusicPlaybackTrack;
import com.hon.fantasy.utils.FantasyUtils;

import java.util.Arrays;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public class MusicPlayer {
    private static volatile MusicPlayer INSTANCE;

    private long[] sEmptyList;
    private ContentValues[] mContentValuesCache = null;

    private IFantasyService mService;
    private ServiceBinder mBinder;

    private MusicPlayer() {
        sEmptyList = new long[0];
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

    public void bindToService(Context context, ServiceConnection callback) {
        mBinder = new ServiceBinder(callback, context);
        context.bindService(new Intent(context, MusicService.class), mBinder, BIND_AUTO_CREATE);
    }

    public void unbindFromService(Context context) {
        if (mBinder != null) {
            context.unbindService(mBinder);
        }
        mService = null;
    }

    public boolean isPlaybackServiceConnected() {
        return mService != null;
    }

    public void next() {
        try {
            if (mService != null) {
                mService.next();
            }
        } catch (RemoteException ignored) {

        }
    }

    public static void asyncNext(Context context) {
        Intent previous = new Intent(context, MusicService.class);
        previous.setAction(MusicService.NEXT_ACTION);
        context.startService(previous);
    }

    public static void previous(Context context, boolean force) {
        Intent previous = new Intent(context, MusicService.class);
        if (force) {
            previous.setAction(MusicService.PREVIOUS_FORCE_ACTION);
        } else {
            previous.setAction(MusicService.PREVIOUS_ACTION);
        }
        context.startService(previous);
    }

    public void playOrPause() {
        try {
            if (mService != null) {
                if (mService.isPlaying()) {
                    mService.pause();
                } else {
                    mService.play();
                }
            }
        } catch (Exception ignored) {

        }
    }

    public void cycleRepeat() {
        try {
            if (mService != null) {
                switch (mService.getRepeatMode()) {
                    case MusicService.REPEAT_NONE:
                        mService.setRepeatMode(MusicService.REPEAT_ALL);
                        break;
                    case MusicService.REPEAT_ALL:
                        mService.setRepeatMode(MusicService.REPEAT_CURRENT);
                        if (mService.getShuffleMode() != MusicService.SHUFFLE_NONE) {
                            mService.setShuffleMode(MusicService.SHUFFLE_NONE);
                        }
                        break;
                    default:
                        mService.setRepeatMode(MusicService.REPEAT_NONE);
                        break;
                }
            }
        } catch (RemoteException ignored) {

        }
    }

    public void cycleShuffle() {
        try {
            if (mService != null) {
                switch (mService.getShuffleMode()) {
                    case MusicService.SHUFFLE_NONE:
                        mService.setShuffleMode(MusicService.SHUFFLE_NORMAL);
                        if (mService.getRepeatMode() == MusicService.REPEAT_CURRENT) {
                            mService.setRepeatMode(MusicService.REPEAT_ALL);
                        }
                        break;
                    case MusicService.SHUFFLE_NORMAL:
                        mService.setShuffleMode(MusicService.SHUFFLE_NONE);
                        break;
                    case MusicService.SHUFFLE_AUTO:
                        mService.setShuffleMode(MusicService.SHUFFLE_NONE);
                        break;
                    default:
                        break;
                }
            }
        } catch (RemoteException ignored) {

        }
    }

    public boolean isPlaying() {
        try {
            if (mService != null) {
                return mService.isPlaying();
            }
        } catch (RemoteException ignored) {

        }
        return false;
    }

    public int getShuffleMode() {
        try {
            if (mService != null) {
                return mService.getShuffleMode();
            }
        } catch (RemoteException ignored) {

        }
        return 0;
    }

    public void setShuffleMode(int mode) {
        try {
            if (mService != null) {
                mService.setShuffleMode(mode);
            }
        } catch (RemoteException ignored) {

        }
    }

    public int getRepeatMode() {
        try {
            if (mService != null) {
                return mService.getRepeatMode();
            }
        } catch (RemoteException ignored) {

        }
        return 0;
    }

    public String getTrackName() {
        if (mService != null) {
            try {
                return mService.getTrackName();
            } catch (RemoteException ignored) {
            }
        }
        return null;
    }

    public String getArtistName() {
        if (mService != null) {
            try {
                return mService.getArtistName();
            } catch (RemoteException ignored) {
            }
        }
        return null;
    }

    public String getAlbumName() {
        if (mService != null) {
            try {
                return mService.getAlbumName();
            } catch (RemoteException ignored) {
            }
        }
        return null;
    }

    public long getCurrentAlbumId() {
        if (mService != null) {
            try {
                return mService.getAlbumId();
            } catch (RemoteException ignored) {
            }
        }
        return -1;
    }

    public long getCurrentAudioId() {
        if (mService != null) {
            try {
                return mService.getAudioId();
            } catch (RemoteException ignored) {
            }
        }
        return -1;
    }

    public MusicPlaybackTrack getCurrentTrack() {
        if (mService != null) {
            try {
                return mService.getCurrentTrack();
            } catch (RemoteException ignored) {
            }
        }
        return null;
    }

    public MusicPlaybackTrack getTrack(int index) {
        if (mService != null) {
            try {
                return mService.getTrack(index);
            } catch (RemoteException ignored) {
            }
        }
        return null;
    }

    public int removeTracks(int first, int last) throws RemoteException {
        return mService.removeTracks(first, last);
    }

    public long getNextAudioId() {
        if (mService != null) {
            try {
                return mService.getNextAudioId();
            } catch (RemoteException ignored) {
            }
        }
        return -1;
    }

    public long getPreviousAudioId() {
        if (mService != null) {
            try {
                return mService.getPreviousAudioId();
            } catch (RemoteException ignored) {
            }
        }
        return -1;
    }

    public long getCurrentArtistId() {
        if (mService != null) {
            try {
                return mService.getArtistId();
            } catch (RemoteException ignored) {
            }
        }
        return -1;
    }

    public int getAudioSessionId() {
        if (mService != null) {
            try {
                return mService.getAudioSessionId();
            } catch (RemoteException ignored) {
            }
        }
        return -1;
    }

    public long[] getQueue() {
        try {
            if (mService != null)
                return mService.getQueue();
        } catch (RemoteException ignored) {
        }
        return sEmptyList;
    }

    public long getQueueItemAtPosition(int position) {
        try {
            if (mService != null) {
                return mService.getQueueItemAtPosition(position);
            } else {
            }
        } catch (RemoteException ignored) {
        }
        return -1;
    }

    public int getQueueSize() {
        try {
            if (mService != null) {
                return mService.getQueueSize();
            } else {
            }
        } catch (RemoteException ignored) {
        }
        return 0;
    }

    public int getQueuePosition() {
        try {
            if (mService != null) {
                return mService.getQueuePosition();
            }
        } catch (RemoteException ignored) {
        }
        return 0;
    }

    public void setQueuePosition(int position) {
        if (mService != null) {
            try {
                mService.setQueuePosition(position);
            } catch (RemoteException ignored) {
            }
        }
    }

    public void refresh() {
        try {
            if (mService != null) {
                mService.refresh();
            }
        } catch (RemoteException ignored) {
        }
    }

    public int getQueueHistorySize() {
        if (mService != null) {
            try {
                return mService.getQueueHistorySize();
            } catch (RemoteException ignored) {
            }
        }
        return 0;
    }

    public int getQueueHistoryPosition(int position) {
        if (mService != null) {
            try {
                return mService.getQueueHistoryPosition(position);
            } catch (RemoteException ignored) {
            }
        }
        return -1;
    }

    public int[] getQueueHistoryList() {
        if (mService != null) {
            try {
                return mService.getQueueHistoryList();
            } catch (RemoteException ignored) {
            }
        }
        return null;
    }

    public int removeTrack(long id) {
        try {
            if (mService != null) {
                return mService.removeTrack(id);
            }
        } catch (RemoteException ingored) {
        }
        return 0;
    }

    public boolean removeTrackAtPosition(long id, int position) {
        try {
            if (mService != null) {
                return mService.removeTrackAtPosition(id, position);
            }
        } catch (RemoteException ingored) {
        }
        return false;
    }

    public void moveQueueItem(int from, int to) {
        try {
            if (mService != null) {
                mService.moveQueueItem(from, to);
            } else {
            }
        } catch (RemoteException ignored) {
        }
    }

    public void playArtist(Context context, long artistId, int position, boolean shuffle) {
        long[] artistList = getSongListForArtist(context, artistId);
        if (artistList != null) {
            playAll(context, artistList, position, artistId, FantasyUtils.IdType.Artist, shuffle);
        }
    }

    public void playAlbum(Context context, long albumId, int position, boolean shuffle) {
        long[] albumList = getSongListForAlbum(context, albumId);
        if (albumList != null) {
            playAll(context, albumList, position, albumId, FantasyUtils.IdType.Album, shuffle);
        }
    }

    public void playAll(Context context, long[] list, int position,
                        long sourceId, FantasyUtils.IdType sourceType,
                        boolean forceShuffle) {
        if (list == null || list.length == 0 || mService == null) {
            return;
        }
        try {
            if (forceShuffle) {
                mService.setShuffleMode(MusicService.SHUFFLE_NORMAL);
            }
            long currentId = mService.getAudioId();
            int currentQueuePosition = getQueuePosition();
            if (position != -1 && currentQueuePosition == position && currentId == list[position]) {
                long[] playlist = getQueue();
                if (Arrays.equals(list, playlist)) {
                    mService.play();
                    return;
                }
            }
            if (position < 0) {
                position = 0;
            }
            mService.open(list, forceShuffle ? -1 : position, sourceId, sourceType.mId);
            mService.play();
        } catch (RemoteException ignored) {
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void playNext(Context context, long[] list, long sourceId, FantasyUtils.IdType sourceType) {
        if (mService == null) {
            return;
        }
        try {
            mService.enqueue(list, MusicService.NEXT, sourceId, sourceType.mId);
            String message = makeLabel(context, R.plurals.NNNtrackstoqueue, list.length);
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } catch (RemoteException ignored) {
        }
    }

    public void shuffleAll(Context context) {
        Cursor cursor = SongLoader.makeSongCursor(context, null, null);
        long[] trackList = SongLoader.getSongListForCursor(cursor);
        if (trackList.length == 0 || mService == null) {
            return;
        }
        try {
            mService.setShuffleMode(MusicService.SHUFFLE_NORMAL);
            if (getQueuePosition() == 0 && mService.getAudioId() == trackList[0] && Arrays.equals(trackList, getQueue())) {
                mService.play();
                return;
            }
            mService.open(trackList, -1, -1, FantasyUtils.IdType.NA.mId);
            mService.play();
            cursor.close();
        } catch (RemoteException ignored) {
        }
    }

    public long[] getSongListForArtist(Context context, long id) {
        String[] projection = new String[]{
                BaseColumns._ID
        };
        String selection = MediaStore.Audio.AudioColumns.ARTIST_ID + "=" + id + " AND "
                + MediaStore.Audio.AudioColumns.IS_MUSIC + "=1";
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null,
                MediaStore.Audio.AudioColumns.ALBUM_KEY + "," + MediaStore.Audio.AudioColumns.TRACK);
        if (cursor != null) {
            long[] mList = SongLoader.getSongListForCursor(cursor);
            cursor.close();
            cursor = null;
            return mList;
        }
        return sEmptyList;
    }

    public long[] getSongListForAlbum(Context context, long id) {
        String[] projection = new String[]{
                BaseColumns._ID
        };
        String selection = MediaStore.Audio.AudioColumns.ALBUM_ID + "=" + id + " AND " + MediaStore.Audio.AudioColumns.IS_MUSIC
                + "=1";
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null,
                MediaStore.Audio.AudioColumns.TRACK + ", " + MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null) {
            long[] mList = SongLoader.getSongListForCursor(cursor);
            cursor.close();
            cursor = null;
            return mList;
        }
        return sEmptyList;
    }

    public int getSongCountForAlbumInt(Context context, long id) {
        int songCount = 0;
        if (id == -1) {
            return songCount;
        }

        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
        Cursor cursor = context.getContentResolver().query(uri,
                new String[]{MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                if (!cursor.isNull(0)) {
                    songCount = cursor.getInt(0);
                }
            }
            cursor.close();
            cursor = null;
        }

        return songCount;
    }

    public String getReleaseDateForAlbum(Context context, long id) {
        if (id == -1) {
            return null;
        }
        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
        Cursor cursor = context.getContentResolver().query(uri, new String[]{
                MediaStore.Audio.AlbumColumns.FIRST_YEAR
        }, null, null, null);
        String releaseDate = null;
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                releaseDate = cursor.getString(0);
            }
            cursor.close();
            cursor = null;
        }
        return releaseDate;
    }

    public void seek(long position) {
        if (mService != null) {
            try {
                mService.seek(position);
            } catch (RemoteException ignored) {
            } catch (IllegalStateException ignored) {

            }
        }
    }

    public void seekRelative(long deltaInMs) {
        if (mService != null) {
            try {
                mService.seekRelative(deltaInMs);
            } catch (RemoteException ignored) {
            } catch (IllegalStateException ignored) {

            }
        }
    }

    public long position() {
        if (mService != null) {
            try {
                return mService.position();
            } catch (RemoteException ignored) {
            } catch (IllegalStateException ex) {

            }
        }
        return 0;
    }

    public long duration() {
        if (mService != null) {
            try {
                return mService.duration();
            } catch (RemoteException ignored) {
            } catch (IllegalStateException ignored) {

            }
        }
        return 0;
    }

    public void clearQueue() {
        if (mService != null) {
            try {
                mService.removeTracks(0, Integer.MAX_VALUE);
            } catch (RemoteException ignored) {
            }
        }
    }

    public void addToQueue(Context context, long[] list, long sourceId,
                           FantasyUtils.IdType sourceType) {
        if (mService == null) {
            return;
        }
        try {
            mService.enqueue(list, MusicService.LAST, sourceId, sourceType.mId);
            String message = makeLabel(context, R.plurals.NNNtrackstoqueue, list.length);
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } catch (RemoteException ignored) {
        }
    }

    public String makeLabel(Context context, int pluralInt,
                            int number) {
        return context.getResources().getQuantityString(pluralInt, number, number);
    }

    public void addToPlaylist(Context context, long[] ids, long playlistid) {
        int size = ids.length;
        ContentResolver resolver = context.getContentResolver();
        String[] projection = new String[]{
                "max(" + "play_order" + ")",
        };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid);
        Cursor cursor = null;
        int base = 0;

        cursor = resolver.query(uri, projection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            base = cursor.getInt(0) + 1;
        }

        if (cursor != null) {
            cursor.close();
        }

        int numinserted = 0;
        for (int offSet = 0; offSet < size; offSet += 1000) {
            makeInsertItems(ids, offSet, 1000, base);
            numinserted += resolver.bulkInsert(uri, mContentValuesCache);
        }
        String message = context.getResources().getQuantityString(
                R.plurals.NNNtrackstoplaylist, numinserted, numinserted);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void makeInsertItems(long[] ids, int offset, int len, int base) {
        if (offset + len > ids.length) {
            len = ids.length - offset;
        }

        if (mContentValuesCache == null || mContentValuesCache.length != len) {
            mContentValuesCache = new ContentValues[len];
        }
        for (int i = 0; i < len; i++) {
            if (mContentValuesCache[i] == null) {
                mContentValuesCache[i] = new ContentValues();
            }
            mContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
            mContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, ids[offset + i]);
        }
    }

    public long createPlaylist(Context context, String name) {
        if (name != null && name.length() > 0) {
            ContentResolver resolver = context.getContentResolver();
            String[] projection = new String[]{
                    MediaStore.Audio.PlaylistsColumns.NAME
            };
            String selection = MediaStore.Audio.PlaylistsColumns.NAME + " = '" + name + "'";
            Cursor cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    projection, selection, null, null);
            if (cursor.getCount() <= 0) {
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);
                Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                        values);
                return Long.parseLong(uri.getLastPathSegment());
            }
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
            return -1;
        }
        return -1;
    }

    public void openFile(String path) {
        if (mService != null) {
            try {
                mService.openFile(path);
            } catch (RemoteException ignored) {
            }
        }
    }

    private class ServiceBinder implements ServiceConnection {
        private ServiceConnection mCallback;
        private Context mContext;


        ServiceBinder(ServiceConnection callback, Context context) {
            mCallback = callback;
            mContext = context;
        }

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IFantasyService.Stub.asInterface(service);
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            mService = null;
        }
    }
}
