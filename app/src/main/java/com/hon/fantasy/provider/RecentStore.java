package com.hon.fantasy.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public class RecentStore {
    private static final int MAX_ITEMS_IN_DB = 100;

    private static RecentStore sInstance = null;

    private MusicDB mMusicDatabase = null;

    public RecentStore(final Context context) {
        mMusicDatabase = MusicDB.getInstance(context);
    }

    public static final synchronized RecentStore getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new RecentStore(context.getApplicationContext());
        }
        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + RecentStoreColumns.NAME + " ("
                + RecentStoreColumns.ID + " LONG NOT NULL," + RecentStoreColumns.TIMEPLAYED
                + " LONG NOT NULL);");
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RecentStoreColumns.NAME);
        onCreate(db);
    }

    public void addSongId(final long songId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();

        try {

            Cursor mostRecentItem = null;
            try {
                mostRecentItem = queryRecentIds("1");
                if (mostRecentItem != null && mostRecentItem.moveToFirst()) {
                    if (songId == mostRecentItem.getLong(0)) {
                        return;
                    }
                }
            } finally {
                if (mostRecentItem != null) {
                    mostRecentItem.close();
                    mostRecentItem = null;
                }
            }


            final ContentValues values = new ContentValues(2);
            values.put(RecentStoreColumns.ID, songId);
            values.put(RecentStoreColumns.TIMEPLAYED, System.currentTimeMillis());
            database.insert(RecentStoreColumns.NAME, null, values);

            Cursor oldest = null;
            try {
                oldest = database.query(RecentStoreColumns.NAME,
                        new String[]{RecentStoreColumns.TIMEPLAYED}, null, null, null, null,
                        RecentStoreColumns.TIMEPLAYED + " ASC");

                if (oldest != null && oldest.getCount() > MAX_ITEMS_IN_DB) {
                    oldest.moveToPosition(oldest.getCount() - MAX_ITEMS_IN_DB);
                    long timeOfRecordToKeep = oldest.getLong(0);

                    database.delete(RecentStoreColumns.NAME,
                            RecentStoreColumns.TIMEPLAYED + " < ?",
                            new String[]{String.valueOf(timeOfRecordToKeep)});

                }
            } finally {
                if (oldest != null) {
                    oldest.close();
                    oldest = null;
                }
            }
        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }


    public void removeItem(final long songId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(RecentStoreColumns.NAME, RecentStoreColumns.ID + " = ?", new String[]{
                String.valueOf(songId)
        });

    }

    public void deleteAll() {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(RecentStoreColumns.NAME, null, null);
    }


    public Cursor queryRecentIds(final String limit) {
        final SQLiteDatabase database = mMusicDatabase.getReadableDatabase();
        return database.query(RecentStoreColumns.NAME,
                new String[]{RecentStoreColumns.ID}, null, null, null, null,
                RecentStoreColumns.TIMEPLAYED + " DESC", limit);
    }

    public interface RecentStoreColumns {
        /* Table name */
        String NAME = "recenthistory";

        /* Album IDs column */
        String ID = "songid";

        /* Time played column */
        String TIMEPLAYED = "timeplayed";
    }
}
