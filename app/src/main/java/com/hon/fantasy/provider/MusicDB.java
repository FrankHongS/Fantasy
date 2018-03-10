package com.hon.fantasy.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public class MusicDB extends SQLiteOpenHelper {

    public static final String DATABASENAME = "musicdb.db";
    private static final int VERSION = 4;
    private static MusicDB sInstance = null;

    private final Context mContext;

    public MusicDB(final Context context) {
        super(context, DATABASENAME, null, VERSION);

        mContext = context;
    }

    public static final synchronized MusicDB getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new MusicDB(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        MusicPlaybackState.getInstance(mContext).onCreate(db);
        RecentStore.getInstance(mContext).onCreate(db);
        SongPlayCount.getInstance(mContext).onCreate(db);
        SearchHistory.getInstance(mContext).onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MusicPlaybackState.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
        RecentStore.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
        SongPlayCount.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
        SearchHistory.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MusicPlaybackState.getInstance(mContext).onDowngrade(db, oldVersion, newVersion);
        RecentStore.getInstance(mContext).onDowngrade(db, oldVersion, newVersion);
        SongPlayCount.getInstance(mContext).onDowngrade(db, oldVersion, newVersion);
        SearchHistory.getInstance(mContext).onDowngrade(db, oldVersion, newVersion);
    }
}
